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

(defn cell [state column row]
  "Cell relative to the active grid"
  (grid/cell (page-coords state) (active-grid state) column row))

(defn absolute-cell [state column row]
  "Cell absolute to the active grid"
  (grid/absolute-cell (active-grid state) column row))

(defn side-cell [state column]
  (side/cell (active-side state) column (grid-y-page state)))

(defn get-row    [state rowIndex] (grid/get-row (page-coords state) (active-grid state) rowIndex))
(defn get-column [state colIndex] (grid/get-column (page-coords state) (active-grid state) colIndex))
(defn absolute-column [state colIndex] (grid/absolute-column (page-coords state) (active-grid state) colIndex))

(defn column-off [state column]
  (let [grid (active-grid state)
        new-grid (reduce (fn [new-grid row] (grid/set-cell (page-coords state) new-grid column row 0))
                         grid
                         (range 0 (count grid)))]
    (swap! state assoc-in [:modes (mode state) :grid] new-grid)))


(defn row-active? [state row]
  (side/on? (active-side state) row (grid-y-page state)))

(defn absolute-row-active? [state row]
  (side/absolute-on? (active-side state) row))

(defn set-page [state x-page y-page] (swap! state assoc :page-coords [x-page y-page]))
(defn set-page-x [state x-page] (set-page state x-page (grid-y-page state)))
(defn set-page-y [state y-page] (set-page state (grid-x-page state) y-page))
(defn reset-page-position [state] (set-page state 0 0))

(defn shift-page-left [state]
  (let [x-page (grid-x-page state)]
    (if (> x-page 0)
      (set-page-x state (dec x-page))
      @state)))

(defn shift-page-right [state]
  (let [current-mode (mode state)
        [x-page y-page] (page-coords state)
        current-x-pages (grid/x-page-count (active-grid state))]
    (when (>= (inc x-page) current-x-pages)
      (swap! state assoc-in [:modes current-mode :grid] (grid/add-page-left (active-grid state))))
    (swap! state assoc :page-coords [(inc x-page) y-page])))

(defn shift-page-up [state]
  (let [y-page (grid-y-page state)]
    (if (> y-page 0)
      (set-page-y state (dec y-page))
      @state)))

(defn shift-page-down [state]
  (let [current-mode (mode state)
        [x-page y-page] (page-coords state)
        current-y-pages (grid/y-page-count (active-grid state))]
    (when (>= (inc y-page) current-y-pages)
      (swap! state assoc-in [:modes current-mode :grid] (grid/add-page-below (active-grid state)))
      (swap! state assoc-in [:modes current-mode :side] (side/add-page-below (active-side state))))
    (swap! state assoc :page-coords [x-page (inc y-page)])))

(defn reset-state! [state] (reset! state (empty-state-map)))

(comment
(def state (atom {:modes {:default {:grid [[0 1 0 0 0 0 0 0] [0 1 0 0 0 0 0 0] [0 1 0 0 0 0 0 0] [0 1 0 0 0 0 0 0]] :side [[1 1][0 0]]}}, :active :default, :page-coords [0 0]}))
)
