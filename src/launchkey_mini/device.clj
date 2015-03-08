(ns launchkey-mini.device
  (:require
    [launchkey-mini.led :as led]
    [launchkey-mini.midi :as midi]
    [launchkey-mini.grid :as grid]))

(defrecord LaunchkeyMini [rcv dev interfaces state])

(def launchkey-mini-config {
  :name "LK Mini InControl"
  :interfaces {
    :grid-controls {
      :meta-keys {
        :up      {:note 104 :type :control-change}
        :down    {:note 105 :type :control-change}
        :left    {:note 106 :type :control-change}
        :right   {:note 107 :type :control-change}}

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
      :midi-handle "LK Mini InControl"
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

(defn reset-launchkey* [sink]
  (midi/send-message sink reset-launchkey-msg))

(defn reset-launchkey [launchkey-mini]
  (reset-launchkey* (-> launchkey-mini :rcv)))

(defn enable-incontrol* [sink]
  (midi/send-message sink enable-incontrol-msg))

(defn enable-incontrol [launchkey-mini]
  (enable-incontrol* (-> launchkey-mini :rcv)))

(defn disable-incontrol* [sink]
  (midi/send-message sink disable-incontrol-msg))

(defn disable-incontrol [launchkey-mini]
  (disable-incontrol* (-> launchkey-mini :rcv)))

(defn led-details [id]
  (if (vector? id)
    {:note (apply grid/coordinate->note id) :fn (midi/midi-fn (-> launchkey-mini-config :interfaces :leds :grid :type))}
    (when-let [config (-> launchkey-mini-config :interfaces :leds :controls id)]
      {:note (config :note) :fn (midi/midi-fn (config :type))})))

(defn led-on*
  ([sink id] (led-on* sink id 3 :amber))
  ([sink id brightness color]
    (when-let [{led-id :note midi-fn :fn} (led-details id)]
      (midi-fn sink led-id (led/velocity {:color color
                                          :intensity brightness})))))

(defn led-on
  ([launchkeymini id] (led-on launchkeymini id led/full-brightness :amber))
  ([launchkeymini id brightness color]
      (let [sink (-> launchkeymini :rcv)]
        (led-on* sink id brightness color))))

(defn led-off* [sink id]
  (when-let [{led-id :note midi-fn :fn} (led-details id)]
    (midi-fn sink led-id 12)))

(defn led-off [launchkeymini id]
  (let [sink (-> launchkeymini :rcv)]
    (led-off* sink id)))

(defn led-flash-on* [sink id brightness color]
  (when-let [{led-id :note midi-fn :fn} (led-details id)]
    (midi-fn sink led-id (led/velocity {:color color :intensity brightness :mode :clear}))
    (Thread/sleep 200)
    (midi-fn sink led-id (led/velocity {:color color :intensity led/off :mode :clear}))))

(defn led-flash-on
  ([launchkeymini id] (led-flash-on launchkeymini id led/full-brightness :amber))
  ([launchkeymini id brightness color]
    (let [sink (-> launchkeymini :rcv)]
      (led-flash-on* sink id brightness color))))

(defn led-on-all* [sink]
  (midi/send-message sink {:type :control-change :note 0x00 :velocity 0x7D}))

(defn led-on-all [launchkeymini]
  (let [sink (-> launchkeymini :rcv)]
    (led-on-all* sink)))

(defn render-row*
  ([sink row-data row] (render-row* sink row-data row led/full-brightness :amber))
  ([sink row-data row brightness color]
    (doseq [column (range 0 grid/grid-width)]
      (let [value (nth row-data column)]
        (if (= value 0)
          (led-off* sink [row column])
          (led-on* sink [row column] brightness color))))))

(defn render-row
  ([launchkeymini row-data row] (render-row launchkeymini row-data row led/full-brightness :amber))
  ([launchkeymini row-data row brightness color]
    (let [sink (-> launchkeymini :rcv)]
      (render-row* sink row-data row brightness color))))

(defn render-side*
  ([sink side-data row] (render-side* sink side-data row led/full-brightness :amber))
  ([sink side-data row brightness color]
    (doseq [row (range 0 grid/grid-height)]
      (let [value (nth side-data row)]
        (if (= value 0)
          (led-off* sink (row->side row))
          (led-on* sink (row->side row) brightness color))))))

(defn render-side
  ([launchkeymini side-data row] (render-side launchkeymini side-data row led/full-brightness :amber))
  ([launchkeymini side-data row brightness color]
    (let [sink (-> launchkeymini :rcv)]
      (render-side* sink side-data row brightness color))))

(defn- id->color [id]
  (nth [:orange :red :green] (mod id 3)))

(defn intromation [sink]
  (reset-launchkey* sink)
  (doall
    (pmap
      (fn [col]
        (let [refresh (+ 50 (rand-int 50))
              start-lag (rand-int 1000)]

          (Thread/sleep start-lag)
          (doseq [color led/led-colors]
            (doseq [brightness (range led/low-brightness (+ led/full-brightness 1))]
              (doseq [row (range 0 grid/grid-height)]
                (led-on* sink [row col] brightness color)
                (Thread/sleep (- refresh row)))))))
      (range 0 grid/grid-width)))
  (led-on-all* sink)
  (Thread/sleep 400)
  (doseq [col (reverse (range 0 grid/grid-width))]
    (doseq [row (reverse (range 0 grid/grid-height))]
      (led-on* sink [row col] led/full-brightness (id->color (+ col (* row grid/grid-width))))
      (Thread/sleep 70)))
  (Thread/sleep 400)
  (doseq [row (reverse (range 0 grid/grid-height))]
    (doseq [col (reverse (range 0 grid/grid-width))]
      (led-off* sink [row col])
      (Thread/sleep 50)))
  (reset-launchkey* sink))
