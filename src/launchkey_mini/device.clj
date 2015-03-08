(ns launchkey-mini.device
  (:require
   [overtone.studio.midi :refer :all]
   [overtone.libs.event :refer :all]

   [launchkey-mini.led :as led]))

(def grid-notes
  [(range 0x60 0x68)
  (range 0x70 0x78)])

(defn- coordinate->note [y x]
  (-> grid-notes (nth (mod y 2)) (nth (mod x 8))))
