(ns launchkey-mini.t-device
  (:require [midje.sweet :refer :all]
            [overtone.studio.midi :refer :all :as midi]
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

(fact "enable-incontrol-msg has correct content"
  (#'device/enable-incontrol-msg :note) => 0x0C
  (#'device/enable-incontrol-msg :velocity) => 0x7F
  (#'device/enable-incontrol-msg :type) => :note-on)

(fact "disable-incontrol-msg has correct content"
  (#'device/disable-incontrol-msg :note) => 0x0C
  (#'device/disable-incontrol-msg :velocity) => 0x0
  (#'device/disable-incontrol-msg :type) => :note-on)

(fact "returns correct led-details"
  (#'device/led-details [0 0]) => {:note 0x60 :type :note-on}
  (#'device/led-details :row1) => {:note 104 :type :note-on}
  (#'device/led-details :foo) => nil)

