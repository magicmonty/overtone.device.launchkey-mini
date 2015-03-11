(ns launchkey-mini.state-maps
  (:require [launchkey-mini.grid :as grid]
            [launchkey-mini.side :as side]))

(defn empty-mode []
  "defines a new empty mode"
  {:grid (grid/empty-grid)
   :side (side/empty-side)})

(defn empty-state-map []
  "defines the initial state"
  {
    :active :default
    :session 0
    :modes {
      :default (empty-mode)
    }
    :fn-map (grid/fn-grid)
    :grid-index [0 0]
  })

(defn mode         [state] (:active @state))
(defn modes        [state] (keys (:modes @state)))
(defn mode?        [state candidate-mode] (= candidate-mode (some #{candidate-mode} (modes state))))
(defn active-mode? [state candidate-mode] (= candidate-mode (mode state)))

(defn add-mode [state mode-id]
  "Adds a new mode to the state"
  (if (mode? state mode-id)
    @state
    (swap! state assoc-in [:modes mode-id] (empty-mode))))

