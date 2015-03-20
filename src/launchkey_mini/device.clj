(ns launchkey-mini.device
  (:use launchkey-mini.util)
  (:require
    [overtone.studio.midi :refer :all :as omidi]
   [overtone.libs.event :refer :all :as oevent]
    [launchkey-mini.led :as led]
    [launchkey-mini.midi :as midi]
    [launchkey-mini.grid :as grid]
    [launchkey-mini.state-maps :as state-maps]))

(def launchkeymini-midi-handle "LK Mini InControl")
(def launchkeymini-event-id :LKMiniInControl)

(defrecord LaunchkeyMini [rcv dev interfaces state])

(def launchkey-mini-config {
  :name launchkeymini-midi-handle
  :interfaces {
    :grid-controls {
      :meta-keys {
        :up      {:note 104 :type :control-change :session-fn state-maps/shift-page-up}
        :down    {:note 105 :type :control-change :session-fn state-maps/shift-page-down}
        :left    {:note 106 :type :control-change :session-fn state-maps/shift-page-left}
        :right   {:note 107 :type :control-change :session-fn state-maps/shift-page-right}}

      :side-controls {
        :row1  {:note 104 :type :note-on :row 0}
        :row2  {:note 120 :type :note-on :row 1}}

      :knobs {
        :knob1   {:note 21  :type :control-change}
        :knob2   {:note 22  :type :control-change}
        :knob3   {:note 23  :type :control-change}
        :knob4   {:note 24  :type :control-change}
        :knob5   {:note 25  :type :control-change}
        :knob6   {:note 26  :type :control-change}
        :knob7   {:note 27  :type :control-change}
        :knob8   {:note 28  :type :control-change}}}

    :leds {
      :name "LEDs"
      :type :midi-out
      :midi-handle launchkeymini-midi-handle
      :controls {
        :row1  {:note 104 :type :note-on}
        :row2  {:note 120 :type :note-on}}
      :grid {
        :type :note-on}}

    :midi-messages {
      :reset-launchkey          {:type :control-change :note 0x00 :velocity 0x00}
      :enable-incontrol         {:type :note-on        :note 0x0C :velocity 0x7F}
      :disable-incontrol        {:type :note-on        :note 0x0C :velocity 0x00}}}})

(def side-controls (-> launchkey-mini-config :interfaces :grid-controls :side-controls keys))
(def knobs (-> launchkey-mini-config :interfaces :grid-controls :knobs keys))
(def meta-keys (-> launchkey-mini-config :interfaces :grid-controls :meta-keys keys))
(defn side->row [name] (-> launchkey-mini-config :interfaces :grid-controls :side-controls name :row))
(defn row->side [row] (if (= row 0) :row1 :row2))

(defn- midi-msg [name] (-> launchkey-mini-config :interfaces :midi-messages name))
(def enable-incontrol-msg (midi-msg :enable-incontrol))
(def disable-incontrol-msg (midi-msg :disable-incontrol))
(def reset-launchkey-msg (midi-msg :reset-launchkey))

(defn- reset-launchkey* [sink]
  (midi/send-message sink reset-launchkey-msg))

(defn reset-launchkey [launchkey-mini]
  "Resets all LED states and the internal clock"
  (reset-launchkey* (-> launchkey-mini :rcv)))

(defn- enable-incontrol* [sink]
  (midi/send-message sink enable-incontrol-msg))

(defn enable-incontrol [launchkey-mini]
  "Enables InControl mode to be able to control the LEDs"
  (enable-incontrol* (-> launchkey-mini :rcv)))

(defn- disable-incontrol* [sink]
  (midi/send-message sink disable-incontrol-msg))

(defn disable-incontrol [launchkey-mini]
  "Disables InControl mode"
  (disable-incontrol* (-> launchkey-mini :rcv)))

(defn- led-details [id]
  (if (vector? id)
    {:note (apply grid/coordinate->note id) :fn (midi/midi-fn (-> launchkey-mini-config :interfaces :leds :grid :type))}
    (when-let [config (-> launchkey-mini-config :interfaces :leds :controls id)]
      {:note (config :note) :fn (midi/midi-fn (config :type))})))

(defn- led-on*
  ([sink id] (led-on* sink id 3 :amber))
  ([sink id brightness color]
    (when-let [{led-id :note midi-fn :fn} (led-details id)]
      (midi-fn sink led-id (led/velocity {:color color
                                          :intensity brightness})))))

(defn led-on
  "turn on a LED via its coordinates ([row col]) for the pads
  or its id (:row1, :row2) for the side buttons.
  By default the color is :amber at full brightness
  You can also provide a brightness (0-3) and a color"
  ([launchkeymini id] (led-on launchkeymini id led/full-brightness :amber))
  ([launchkeymini id brightness color]
      (let [sink (-> launchkeymini :rcv)]
        (led-on* sink id brightness color))))

(defn- led-off* [sink id]
  (when-let [{led-id :note midi-fn :fn} (led-details id)]
    (midi-fn sink led-id 12)))

(defn led-off [launchkeymini id]
  "turn of a LED via its coordinates ([row col]) for the pads
  or its id (:row1, :row2) for the side buttons."
  (let [sink (-> launchkeymini :rcv)]
    (led-off* sink id)))

(defn toggle-led
  ([launchkeymini id cell-value] (toggle-led launchkeymini id cell-value led/full-brightness :amber))
  ([launchkeymini id cell-value brightness color]
     (if-not (= 0 cell-value)
       (led-on launchkeymini id brightness color)
       (led-off launchkeymini id))))

(defn- led-flash-on* [sink id brightness color]
  (when-let [{led-id :note midi-fn :fn} (led-details id)]
    (midi-fn sink led-id (led/velocity {:color color :intensity brightness :mode :clear}))
    (Thread/sleep 200)
    (midi-fn sink led-id (led/velocity {:color color :intensity led/off :mode :clear}))))

(defn led-flash-on
  "briefly flash a LED via its coordinates ([row col]) for the pads
  or its id (:row1, :row2) for the side buttons."
  ([launchkeymini id] (led-flash-on launchkeymini id led/full-brightness :amber))
  ([launchkeymini id brightness color]
    (let [sink (-> launchkeymini :rcv)]
      (led-flash-on* sink id brightness color))))

(defn- led-on-all* [sink]
  (midi/send-message sink {:type :control-change :note 0x00 :velocity 0x7D}))

(defn led-on-all [launchkeymini]
  "Enable LED test mode, wich switches all LEDs on (:amber at medium brightness)"
  (let [sink (-> launchkeymini :rcv)]
    (led-on-all* sink)))

(defn- render-row*
  ([sink row-data row] (render-row* sink row-data row led/full-brightness :amber))
  ([sink row-data row brightness color]
    (doseq [column (range 0 grid/grid-width)]
      (let [value (nth row-data column)]
        (if (= value 0)
          (led-off* sink [column row])
          (led-on* sink [column row] brightness color))))))

(defn render-row
  "Renders a complete row. You have to provide the row-data
  as seqence of length launchkey-mini.grid/grid-length
  and a row number (zero based)"
  ([launchkeymini row] (render-row launchkeymini row led/full-brightness :amber))
  ([launchkeymini row brightness color]
     (let [grid (seq (state-maps/active-page (:state launchkeymini)))]
       (doseq [column (range grid/grid-width)]
         (toggle-led launchkeymini [column row] (grid/cell grid column row) brightness color))))

  ([launchkeymini row-data row] (render-row launchkeymini row-data row led/full-brightness :amber))
  ([launchkeymini row-data row brightness color]
    (let [sink (-> launchkeymini :rcv)]
      (render-row* sink row-data row brightness color))))

(defn render-grid
  "Renders a complete visible grid."
  ([launchkeymini grid] (render-grid launchkeymini grid led/full-brightness :amber))
  ([launchkeymini grid brightness color]
    (doseq [row (range 0 grid/grid-height)]
      (let [row-data (nth grid row)]
        (render-row launchkeymini row-data row brightness color)))))

(defn- render-side*
  ([sink side-data] (render-side* sink side-data led/full-brightness :amber))
  ([sink side-data brightness color]
    (doseq [row (range 0 grid/grid-height)]
      (let [value (nth side-data row)]
        (if (= value 0)
          (led-off* sink (row->side row))
          (led-on* sink (row->side row) brightness color))))))

(defn render-side
  "Renders the side buttons. You have to provide the side-data
  as seqence of length launchkey-mini.grid/grid-height"
  ([launchkeymini side-data] (render-side launchkeymini side-data led/full-brightness :amber))
  ([launchkeymini side-data brightness color]
    (let [sink (-> launchkeymini :rcv)]
      (render-side* sink side-data brightness color))))

(defn led-off-all* [sink]
  (doseq [row (range 0 grid/grid-height)]
    (doseq [column (range 0 grid/grid-width)]
      (led-off* sink [column row])))
  (doseq [ctrl side-controls]
    (led-off* sink ctrl)))

(defn led-off-all [launchkeymini]
  "Enable LED test mode, wich switches all LEDs on (:amber at medium brightness)"
  (let [sink (-> launchkeymini :rcv)]
    (led-off-all* sink)))

(defn render-state
  "Renders the current state (grid and side)"
  ([launchkeymini] (render-state launchkeymini led/full-brightness :amber))
  ([launchkeymini brightness color]
     (if (state-maps/session-mode? (:state launchkeymini))
       (let [grid-data (state-maps/active-page (:state launchkeymini))
             side-data (state-maps/active-side-page (:state launchkeymini))]
         (render-grid launchkeymini grid-data brightness color)
         (render-side launchkeymini side-data brightness color))

       (led-off-all launchkeymini))))

(defn- id->color [id]
  (nth [:orange :red :green] (mod id 3)))

(defn- intromation* [sink]
  "Runs a nice intromation on the device"
  (enable-incontrol* sink)
  (reset-launchkey* sink)
  (doall
    (pmap
      (fn [col]
        (let [refresh (+ 50 (rand-int 50))
              start-lag (rand-int 1000)]

          (Thread/sleep start-lag)
          (doseq [color led/led-colors]
            (doseq [brightness (range led/low-brightness (+ led/full-brightness 1))]
              (doseq [row-side side-controls]
                (led-on* sink row-side brightness color)
                (Thread/sleep (- refresh (side->row row-side))))
              (doseq [row (range 0 grid/grid-height)]
                (led-on* sink [col row] brightness color)
                (Thread/sleep (- refresh row)))))))
      (range 0 grid/grid-width)))
  (Thread/sleep 400)
  (doseq [col (reverse (range 0 grid/grid-width))]
    (doseq [row (reverse (range 0 grid/grid-height))]
      (led-on* sink [col row] led/full-brightness (id->color (+ col (* row grid/grid-width))))
      (Thread/sleep 70)))
  (Thread/sleep 400)
  (doseq [row (reverse (range 0 grid/grid-height))]
    (doseq [col (reverse (range 0 grid/grid-width))]
      (led-off* sink [col row])
      (Thread/sleep 50)))
  (reset-launchkey* sink))

(defn- make-grid-on-event-handler [launchkeymini idx state column row note]
  (fn [_]
    (state-maps/toggle! state column row)
    (if (state-maps/session-mode? state (state-maps/mode state))
      (toggle-led launchkeymini [column row] (state-maps/cell state column row))
      (led-on launchkeymini [column row] led/full-brightness :red))
    (when-let [trigger-fn (state-maps/trigger-fn state column row)]
      (if (= 0 (arg-count trigger-fn))
        (trigger-fn)
        (trigger-fn launchkeymini)))
    (let [current-mode (state-maps/mode state)]
      (event [launchkeymini-event-id idx current-mode :grid-on]
             :id [column row]
             :note note
             :launchkeymini launchkeymini
             :idx idx))))

(defn- make-grid-off-event-handler [launchkeymini idx state column row note]
  (fn [_]
    (let [current-mode (state-maps/mode state)]
      (when-not (state-maps/session-mode? state current-mode)
        (led-off launchkeymini [column row]))
      (event [launchkeymini-event-id idx current-mode :grid-off]
             :id [column row]
             :note note
             :launchkeymini launchkeymini
             :idx idx))))

(defn- bind-grid-events [launchkeymini device-key idx state]
  (doseq [[row notes] (map vector (iterate inc 0) (grid/get-page grid/grid-notes))
          [column note] (map vector (iterate inc 0) notes)]
    (let [type       :note-on
          note       note
          on-handle  (concat device-key [type note])
          on-fn (make-grid-on-event-handler launchkeymini idx state column row note)
          off-handle (concat device-key [:note-off note])
          off-fn (make-grid-off-event-handler launchkeymini idx state column row note)]

      (println :handle on-handle)
      (println :handle off-handle)

      (oevent/on-event on-handle  on-fn  (str "grid-on-event-for"  on-handle))
      (oevent/on-event off-handle off-fn (str "grid-off-event-for" off-handle)))))


(defn- make-side-event-handler [launchkeymini id state]
  (fn [_]
    (state-maps/toggle-side! state (side->row id))
    (toggle-led launchkeymini id (state-maps/side-cell state (side->row id)))
    (when-let [trigger-fn (state-maps/trigger-fn state id)]
      (if (= 0 (arg-count trigger-fn))
        (trigger-fn)
        (trigger-fn launchkeymini)))))

(defn- bind-side-events [launchkeymini device-key interfaces state]
  (doseq [[id side-info] (-> interfaces :grid-controls :side-controls)]
    (let [type      (:type side-info)
          note      (:note side-info)
          row       (:row side-info)
          on-handle (concat device-key [type note])
          on-fn (make-side-event-handler launchkeymini id state)]
      (println :handle on-handle)
      (oevent/on-event on-handle on-fn (str "side-on-event-for" on-handle)))))

(defn- on-metakey-on [launchkeymini idx interfaces id value]
  (let [session-fn (-> interfaces :grid-controls :meta-keys id :session-fn)
        current-state (:state launchkeymini)
        current-mode (state-maps/mode current-state)]
    (if (state-maps/session-mode? current-state)
      ; paging in session mode
      ((session-fn current-state)
      (state-maps/print-current-page current-state)
      (render-state launchkeymini))

      ;bindable if not in session mode
      (when-let [trigger-fn (state-maps/trigger-fn current-state id)]
         (if (= 0 (arg-count trigger-fn))
           (trigger-fn)
           (trigger-fn launchkeymini))))

    (event [launchkeymini-event-id idx current-mode :meta (keyword (subs (str id "-on") 1))]
           :val value
           :id id
           :launchkeymini launchkeymini
           :idx idx)))

(defn- on-metakey-off [launchkeymini idx id value]
  (let [current-state (:state launchkeymini)
        current-mode (state-maps/mode current-state)]
    (event [launchkeymini-event-id idx current-mode :meta (keyword (subs (str id "-off") 1))]
           :val value
           :id id
           :launchkeymini launchkeymini
           :idx idx)))

(defn- bind-metakey-events [launchkeymini device-key idx interfaces]
  (doseq [[id meta-key-info] (-> interfaces :grid-controls :meta-keys)]
    (let [current-state (:state launchkeymini)
          current-mode  (state-maps/mode current-state)
          type          (:type meta-key-info)
          note          (:note meta-key-info)
          on-handle     (concat device-key [type note])
          on-fn (fn [{:keys [data2-f]}]
            (if (zero? data2-f)
              (on-metakey-off launchkeymini idx id data2-f)
              (on-metakey-on  launchkeymini idx interfaces id data2-f))

              (event [launchkeymini-event-id idx current-state :meta id]
                      :val data2-f
                      :id id
                      :launchkeymini launchkeymini
                      :idx idx))]

      (println :handle on-handle)
      (oevent/on-event on-handle on-fn (str "metakey-on-event-for" on-handle)))))

(defn- register-event-handlers-for-launchkeymini [device sink idx]
  (let [launchkeymini  (map->LaunchkeyMini (assoc device :rcv sink))
        interfaces     (:interfaces device)
        device-key     (midi-full-device-key (:dev device))
        state          (:state device)]
    (bind-grid-events    launchkeymini device-key idx state)
    (bind-side-events    launchkeymini device-key interfaces state)
    (bind-metakey-events launchkeymini device-key idx interfaces)
    launchkeymini))

(defn stateful-launchkeymini [device]
  (let [interfaces (-> launchkey-mini-config :interfaces)
        state      (atom (state-maps/empty-state-map))
        device-key (omidi/midi-full-device-key device)]
    {:dev        device
     :interfaces interfaces
     :state      state
     :type       ::stateful-launchkeymini}))

(defn merge-launchkeymini-kons [sinks stateful-devs]
  (doseq [sink sinks]
    (enable-incontrol* sink)
    (reset-launchkey* sink)
    (intromation* sink))
  (doall
    (map (fn [[stateful-dev sink id]]
      (register-event-handlers-for-launchkeymini stateful-dev sink id))
    (map vector stateful-devs sinks (range)))))
