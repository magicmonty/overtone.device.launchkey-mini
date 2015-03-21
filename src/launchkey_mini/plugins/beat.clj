(ns launchkey-mini.plugins.beat
  "Use LEDS in a row to express points beat should strike.
   When playing scroll through > 8 phrase beats. Either
   edit beats live as they play or switch to session mode and
   edit each grid."
  (:require
   [launchkey-mini.led :as led]
   [launchkey-mini.device :as device]
   [launchkey-mini.state-maps :as state-maps]
   [launchkey-mini.grid :as grid]))


(defn render-state-with-beat [launchkeymini beat-no]
  (device/render-state launchkeymini)
  (let [beat-column (mod beat-no grid/grid-width)
        current-state (:state launchkeymini)
        grid (state-maps/active-grid current-state)]
    (doseq [row (range 0 grid/grid-height)]
      (let [cell (grid/cell grid beat-column row)
            brigthness (if (= cell 0) led/low-brightness led/full-brightness)
            color (if (state-maps/row-active? current-state row) :green :red)]
        (device/led-on launchkeymini [beat-column row] brigthness color)))))
