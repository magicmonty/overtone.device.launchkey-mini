(ns launchkey-mini.core
  (:use
   [slingshot.slingshot  :only [throw+]])
  (:require
   [overtone.studio.midi      :as midi]
   [overtone.libs.event       :as e]
   [launchkey-mini.state-maps :as state-maps]
   [launchkey-mini.device     :as device]
   [launchkey-mini.grid       :as g]))

(defn boot-launchkey-mini! []
  "Setup event bindings for Novation Launchkey Mini devices."

  (defonce launchkeymini-connected-receivers (midi/midi-find-connected-receivers device/launchkeymini-midi-handle))
  (defonce launchkeymini-connected-devices   (midi/midi-find-connected-devices device/launchkeymini-midi-handle))
  (defonce launchkeymini-stateful-devices    (map device/stateful-launchkeymini launchkeymini-connected-devices))
  (defonce launchkeymini-kons                (device/merge-launchkeymini-kons launchkeymini-connected-receivers launchkeymini-stateful-devices))

  (when (empty? launchkeymini-kons)
    (throw+ {:type ::LaunchkeyMiniNotFound :hint "No Launchkey Mini connected. Is it plugged in?"}))

  (defn bind
    "For a specific mode bind a grid cell key press to a function.
     If function takes an argument it will be passed a stateful launchkey device.

     To bind a button that is not on the 0 y axis/page specify the mode as:
     [:default 1]"
     ([cell fun] (bind (first launchkeymini-kons) (state-maps/mode (:state (first launchkeymini-kons))) cell fun))
     ([mode cell fun] (bind (first launchkeymini-kons) mode cell fun))
     ([launchkeymini mode cell fun]
      (let [[mode page] (if (sequential? mode) mode [mode 0])
            current-state (:state launchkeymini)
            bind-handle (state-maps/handle-key current-state mode [0 page])]
        (assert (state-maps/mode? current-state mode))
        (swap! current-state assoc-in [:fn-map bind-handle cell] fun)))))

(comment
  (use 'launchkey-mini.core)
  (use 'overtone.live)
  (boot-launchkey-mini!)
  )
