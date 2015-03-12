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
    :page-coords [0 0]
  })

(defn mode         [state] (:active @state))
(defn modes        [state] (keys (:modes @state)))
(defn mode?        [state candidate-mode] (= candidate-mode (some #{candidate-mode} (modes state))))
(defn active-mode? [state candidate-mode] (= candidate-mode (mode state)))
(defn active-mode  [state] (-> ((mode state) (:modes @state))))
(defn page-coords  [state] (:page-coords @state))
(defn- grid-y-page [state] (second (page-coords state)))
(defn- grid-x-page [state] (first (page-coords state)))
(defn- page-id     [state] (str (grid-x-page state) "x" (grid-y-page state)))

(defn add-mode [state mode-id]
  "Adds a new mode to the state"
  (if (mode? state mode-id)
    @state
    (swap! state assoc-in [:modes mode-id] (empty-mode))))

(defn active-side [state] ((active-mode state) :side))
(defn active-grid [state] ((active-mode state) :grid))
(defn active-page [state] (grid/get-page (page-coords state) (active-grid state)))

(defn on?
  ([state column row] (on? state column row (page-coords state)))
  ([state column row page-coords] (grid/on? page-coords (active-grid state) column row)))

(defn visible? [state column row]
  (let [[x-page y-page] (page-coords state)]
    (and (= x-page (int (/ column grid/grid-width)))
         (= y-page (int (/ row grid/grid-height))))))


(defn- active-handle-key [state] (keyword (subs (str (mode state) "-" (page-id state)) 1)))
(defn trigger-fn
  ([state column row] (trigger-fn state (str column "x" row)))
  ([state id]
    (get-in (:fn-map @state) [(active-handle-key state) (keyword id)])))

(defn toggle! [state column row]
  (let [new-grid (grid/toggle (page-coords state) (active-grid state) column row)]
    (swap! state assoc-in [:modes (mode state) :grid] new-grid)
    state))

(defn toggle-side! [state row]
  (let [new-side (side/toggle (active-side state) row (grid-y-page state))]
    (swap! state assoc-in [:modes (mode state) :side] new-side)
    state))

(defn set-cell! [state column row value]
  (swap! state assoc-in [:modes (mode state) :grid] (grid/set-cell (page-coords state) (active-grid state) column row value))
  state)

