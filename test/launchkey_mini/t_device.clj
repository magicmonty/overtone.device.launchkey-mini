(ns launchkey-mini.t-device
  (:require [midje.sweet :refer :all]
            [overtone.studio.midi :refer :all :as midi]
            [launchkey-mini.device :as device]))

(background
  (#'midi/midi-note-on anything anything anything anything) => false
  (#'midi/midi-note-on anything anything anything) => false
  (#'midi/midi-control anything anything anything anything) => false
  (#'midi/midi-control anything anything anything) => false)


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

(fact "enable-incontrol sends note on to note 0x0C with velocity 0x7F"
  (#'device/enable-incontrol {:rcv ..sink..}) => truthy
  (provided
    (#'midi/midi-note-on ..sink.. 0x0C 0x7F) => true))

(fact "reset-launchkey sends control change to note 0 with velocity 0"
  (#'device/reset-launchkey {:rcv ..sink..}) => truthy
  (provided
    (#'midi/midi-control ..sink.. 0 0) => true))

(fact "disable-incontrol sends note on to note 0x0C with velocity 0x00"
  (#'device/disable-incontrol {:rcv ..sink..}) => truthy
  (provided
    (#'midi/midi-note-on ..sink.. 0x0C 0x00) => true))

(fact "led-on sends note-on amber at full brightness by default for a grid entry"
  (#'device/led-on {:rcv ..sink..} [0 0]) => truthy
  (provided
    (#'midi/midi-note-on ..sink.. 0x60 63) => true)

  (#'device/led-on {:rcv ..sink..} [7 0]) => truthy
  (provided
    (#'midi/midi-note-on ..sink.. 0x67 63) => true)

  (#'device/led-on {:rcv ..sink..} [0 1]) => truthy
  (provided
    (#'midi/midi-note-on ..sink.. 0x70 63) => true)

  (#'device/led-on {:rcv ..sink..} [7 1]) => truthy
  (provided
    (#'midi/midi-note-on ..sink.. 0x77 63) => true))

(fact "led-on sends note-on with correct color for a grid entry"
  (#'device/led-on {:rcv ..sink..} [0 0] 3 :red) => truthy
  (provided
    (#'midi/midi-note-on ..sink.. 0x60 15) => true))

(fact "led-on sends note-on with for a side button"
  (#'device/led-on {:rcv ..sink..} :row1) => truthy
  (provided
    (#'midi/midi-note-on ..sink.. 104 63) => true)

  (#'device/led-on {:rcv ..sink..} :row2) => truthy
  (provided
    (#'midi/midi-note-on ..sink.. 120 63) => true))

(fact "led-off sends note-on with velocity 12"
  (#'device/led-off {:rcv ..sink..} [0 0]) => truthy
  (provided
    (#'midi/midi-note-on ..sink.. 0x60 12) => true)

  (#'device/led-off {:rcv ..sink..} :row1) => truthy
  (provided
    (#'midi/midi-note-on ..sink.. 104 12) => true))

(fact "led-on-all sends midi-control to note 0 with velocity 0x7D"
  (#'device/led-on-all {:rcv ..sink..}) => truthy
  (provided
    (#'midi/midi-control ..sink.. 0x00 0x7D) => true))

(fact "render-row calls the correct commands"
  (#'device/render-row {:rcv ..sink..} [0 1 0 0 0 1 0 0] 0) => nil
  (provided
    (#'device/led-off* ..sink.. [0 0]) => true
    (#'device/led-on*  ..sink.. [1 0] 3 :amber) => true
    (#'device/led-off* ..sink.. [2 0]) => true
    (#'device/led-off* ..sink.. [3 0]) => true
    (#'device/led-off* ..sink.. [4 0]) => true
    (#'device/led-on*  ..sink.. [5 0] 3 :amber) => true
    (#'device/led-off* ..sink.. [6 0]) => true
    (#'device/led-off* ..sink.. [7 0]) => true))

(fact "render-side calls the correct commands"
  (#'device/render-side {:rcv ..sink..} [0 1]) => nil
  (provided
    (#'device/led-off* ..sink.. :row1) => true
    (#'device/led-on*  ..sink.. :row2 3 :amber) => true))
