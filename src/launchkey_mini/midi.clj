(ns launchkey-mini.midi
  (:require [overtone.studio.midi :refer :all]))

(defn midi-fn [type]
  (case type
    :note-on midi-note-on
    :note-off midi-note-off
    :control-change midi-control
    nil))

(defn send-message
  ([sink message] (send-message sink message 0))
  ([sink message channel]
    (when-let [mfn (midi-fn (message :type))]
      (mfn sink (message :note) (message :velocity)))))
