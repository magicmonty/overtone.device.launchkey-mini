(ns launchkey-mini.mode
  (:require [launchkey-mini.state-maps :as state-maps]
            [launchkey-mini.device :as device]
            [launchkey-mini.core :refer :all]))


(defn mode?
  ([mode-id] (mode? (first launchkeymini-kons) mode-id))
  ([launchkeymini mode-id] (state-maps/mode? (:state launchkeymini) mode-id)))

(defn add-mode!
  ([mode-id] (add-mode! (first launchkeymini-kons) mode-id))
  ([launchkeymini mode-id] (state-maps/add-mode (:state launchkeymini) mode-id)))

(defn enable-session-mode
  ([mode-id] (enable-session-mode (first launchkeymini-kons) mode-id))
  ([launchkeymini mode-id]
    ((state-maps/enable-session-mode! (:state launchkeymini) mode-id)
     (device/render-state launchkeymini))))

(defn disable-session-mode
  ([mode-id] (disable-session-mode (first launchkeymini-kons) mode-id))
  ([launchkeymini mode-id]
    ((state-maps/disable-session-mode! (:state launchkeymini) mode-id)
     (device/render-state launchkeymini))))

(defn set-mode
  ([mode-id] (set-mode (first launchkeymini-kons) mode-id))
  ([launchkeymini mode-id]
    (let [state (:state launchkeymini)]
      (state-maps/reset-page-position state)
      (swap! state assoc :active mode-id))
    (device/render-state launchkeymini)))

(defn set-page-max-x
  ([mode-id value] (set-page-max-x (first launchkeymini-kons) mode-id value))
  ([launchkeymini mode-id value] (state-maps/set-page-max-x (:state launchkeymini) mode-id value)))

(defn set-page-max-y
  ([mode-id value] (set-page-max-y (first launchkeymini-kons) mode-id value))
  ([launchkeymini mode-id value] (state-maps/set-page-max-y (:state launchkeymini) mode-id value)))
