(ns launchkey-mini.led
  (:require
   [overtone.studio.midi :refer :all]
   [overtone.libs.event :refer :all]))

(defprotocol LED
  (led-on [this id]
          [this id color]
          [this id color brightness])
  (led-flash-on [this id]
                [this id color]
                [this id color brightness])
  (led-off [this id])
  (led-on-all [this])
  (led-off-all [this]))

(def off               0)
(def low-brightness    1)
(def medium-brightness 2)
(def full-brightness   3)

(def led-colors [:red :green :yellow :orange :amber])

(def flags {:ignore 0
            :clear 8
            :copy 12})

(defn velocity [{color :color intensity :intensity mode :mode}]
  (if (some #{color} led-colors)
    (let [intensity (if (> intensity 3) 3 intensity)
          green (case color
                  :green intensity
                  :yellow intensity
                  :orange 2
                  :amber intensity
                  0)
          red (case color
                :red intensity
                :yellow 2
                :orange intensity
                :amber intensity
                0)
          mode (or mode :copy)]
      (+ (* 16 green)
         red
         (mode flags)))
    0))
