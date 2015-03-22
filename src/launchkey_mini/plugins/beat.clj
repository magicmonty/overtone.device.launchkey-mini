(ns launchkey-mini.plugins.beat
  "Use LEDS in a row to express points beat should strike.
   When playing scroll through > 8 phrase beats. Either
   edit beats live as they play or switch to session mode and
   edit each grid."
  (:use [launchkey-mini.plugins.sequencer])
  (:require
   [overtone.libs.event       :as e]
   [overtone.sc.trig          :as trig]
   [launchkey-mini.led        :as led]
   [launchkey-mini.device     :as device]
   [launchkey-mini.state-maps :as state-maps]
   [launchkey-mini.grid       :as grid]))

(def phrase-size 16)

(defn toggle-row [launchkeymini sequencer absolute-row]
  (let [state (:state launchkeymini)]
    (if (state-maps/absolute-row-active? state absolute-row)
      (sequencer-write! sequencer absolute-row (take phrase-size (state-maps/complete-row state absolute-row)))
      (reset-pattern! sequencer absolute-row))))

(defn toggle-rows [launchkeymini sequencer]
  (doseq [absolute-row (range 0 (state-maps/y-max (:state launchkeymini)))]
    (toggle-row launchkeymini sequencer absolute-row)))

(defn render-state-with-beat
  ([launchkeymini mode-id]
   (let [state (:state launchkeymini)]
     (when-let [current-beat (:beat @state)]
       (render-state-with-beat launchkeymini mode-id current-beat))))

  ([launchkeymini mode-id beat-no]
   (when (state-maps/active-mode? (:state launchkeymini) mode-id)
     (device/render-state launchkeymini)
     (let [beat-column (mod beat-no grid/grid-width)
           absolute-column (mod beat-no phrase-size)
           x-page (int (/ absolute-column grid/grid-width))
           current-state (:state launchkeymini)
           grid (state-maps/active-grid current-state)]
       (doseq [row (range 0 grid/grid-height)]
         (let [cell (grid/cell grid beat-column row)
               brigthness (if (= cell 0) led/low-brightness led/full-brightness)
               color (if (state-maps/row-active? current-state row) :green :red)]
           (when (= x-page (state-maps/grid-x-page current-state))
             (device/led-on launchkeymini [beat-column row] brigthness color))))))))

(defn update-current-beat [launchkeymini mode-id beat-no]
  (let [state (:state launchkeymini)]
    (render-state-with-beat launchkeymini mode-id)
    (swap! state assoc :beat beat-no)))

(defn add-beat-event-handlers [launchkeymini idx mode-id count-trig-id sequencer]
  (let [state (:state launchkeymini)]
    (trig/on-trigger count-trig-id
                     (fn [beat] (update-current-beat launchkeymini mode-id beat))
                     (str "beat-trigger-for-" idx))

    (e/on-event [device/launchkeymini-event-id idx mode-id :side]
                (fn [_]
                  (render-state-with-beat launchkeymini mode-id)
                  (toggle-rows launchkeymini sequencer))
                (str "side-beat-event-for-" idx))

    (e/on-event [device/launchkeymini-event-id idx mode-id :grid-on]
                (fn [_]
                  (render-state-with-beat launchkeymini mode-id)
                  (toggle-rows launchkeymini sequencer))
                (str "grid-beat-event-for-" idx))

    (e/on-event [device/launchkeymini-event-id idx mode-id :meta :up]
                (fn [_] (render-state-with-beat launchkeymini mode-id))
                (str "meta-up-beat-event-for-" idx))

    (e/on-event [device/launchkeymini-event-id idx mode-id :meta :down]
                (fn [_] (render-state-with-beat launchkeymini mode-id))
                (str "meta-down-beat-event-for-" idx))

    (e/on-event [device/launchkeymini-event-id idx mode-id :meta :left]
                (fn [_] (render-state-with-beat launchkeymini mode-id))
                (str "meta-left-beat-event-for-" idx))

    (e/on-event [device/launchkeymini-event-id idx mode-id :meta :right]
                (fn [_] (render-state-with-beat launchkeymini mode-id))
                (str "meta-right-beat-event-for-" idx))
    ))

(defn remove-beat-event-handlers [idx]
  (e/remove-event-handler (str "beat-trigger-for-" idx))
  (e/remove-event-handler (str "meta-up-beat-event-for-" idx))
  (e/remove-event-handler (str "meta-down-beat-event-for-" idx))
  (e/remove-event-handler (str "meta-left-beat-event-for-" idx))
  (e/remove-event-handler (str "meta-right-beat-event-for-" idx))
  (e/remove-event-handler (str "side-beat-event-for-" idx))
  (e/remove-event-handler (str "grid-beat-event-for-" idx)))

(defn off [sequencer]
  (reset-all-patterns! sequencer))
