(ns launchkey-mini.t-device
  (:require [midje.sweet :refer :all]
            [launchkey-mini.device :as device]))

(fact "correct side controls"
  device/side-controls => (just [:row1 :row2] :in-any-order))

(fact "correct knobs"
  device/knobs => (just [:knob1 :knob2 :knob3 :knob4 :knob5 :knob6 :knob7 :knob8] :in-any-order))

(fact "correct meta keys"
  device/meta-keys => (just [:up :down :left :right] :in-any-order))

(fact "side->row returns correct row"
  (device/side->row :row1) => 0
  (device/side->row :row2) => 1)

(fact "side->row returns nil on wrong row"
  (device/side->row :knob1) => nil)
