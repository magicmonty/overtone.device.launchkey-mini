(ns launchkey-mini.device
  (:require
   [overtone.studio.midi :refer :all]
   [overtone.libs.event :refer :all]

   [launchkey-mini.led :as led]))

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
        :row1  {:note 104 :fn midi-note-on}
        :row2  {:note 120 :fn midi-note-on}}
      :grid {
        :fn midi-note-on}}}})

(def side-controls (-> launchkey-mini-config :interfaces :grid-controls :side-controls keys))
(def knobs (-> launchkey-mini-config :interfaces :grid-controls :knobs keys))
(def meta-keys (-> launchkey-mini-config :interfaces :grid-controls :meta-keys keys))

(defn side->row [name] (-> launchkey-mini-config :interfaces :grid-controls :side-controls name :row))
