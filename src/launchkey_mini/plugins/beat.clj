(ns launchkey-mini.plugins.beat
  "Use LEDS in a row to express points beat should strike.
   When playing scroll through > 8 phrase beats. Either
   edit beats live as they play or switch to session mode and
   edit each grid."
  (:require
   [overtone.libs.event       :as e]
   [launchkey-mini.led        :as led]
   [launchkey-mini.device     :as device]
   [launchkey-mini.state-maps :as state-maps]
   [launchkey-mini.grid       :as grid]))


(defn render-state-with-beat
  ([launchkeymini]
   (let [state (:state launchkeymini)]
     (when-let [current-beat (:beat @state)]
       (render-state-with-beat launchkeymini current-beat))))

  ([launchkeymini beat-no]
   (device/render-state launchkeymini)
   (let [beat-column (mod beat-no grid/grid-width)
         current-state (:state launchkeymini)
         grid (state-maps/active-grid current-state)]
     (doseq [row (range 0 grid/grid-height)]
       (let [cell (grid/cell grid beat-column row)
             brigthness (if (= cell 0) led/low-brightness led/full-brightness)
             color (if (state-maps/row-active? current-state row) :green :red)]
         (device/led-on launchkeymini [beat-column row] brigthness color))))))

(defn add-beat-event-handlers [launchkeymini idx mode-id metro]
  (let [state (:state launchkeymini)]
    (when-let [current-beat (:beat @state)]
      (e/on-event [device/launchkeymini-event-id idx mode-id :side]
                  (fn [_] (render-state-with-beat launchkeymini current-beat))
                  (str "side-beat-event-for-" idx))

      (e/on-event [device/launchkeymini-event-id idx mode-id :grid-on]
                  (fn [_] (render-state-with-beat launchkeymini current-beat))
                  (str "grid-beat-event-for-" idx)))))

(defn update-current-beat [launchkeymini beat-no]
  (let [state (:state launchkeymini)]
    (swap! state assoc :beat beat-no)))

(defn remove-beat-event-handlers [idx]
  (e/remove-event-handler (str "side-beat-event-for-" idx))
  (e/remove-event-handler (str "grid-beat-event-for-" idx)))

