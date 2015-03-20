(ns launchkey-mini.state-maps
  (:require [launchkey-mini.grid :as grid]
            [launchkey-mini.side :as side]))

(defn empty-mode []
  "defines a new empty mode"
  {:session? false
   :grid (grid/empty-grid)
   :side (side/empty-side)})

(defn empty-state-map []
  "defines the initial state"
  {
    :active :default
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
(defn grid-y-page [state] (second (page-coords state)))
(defn grid-x-page [state] (first (page-coords state)))
(defn page-id      [state] (str (grid-x-page state) "x" (grid-y-page state)))

(defn print-current-page [state]
  (println (str "Mode: " (mode state) " / Page: " (page-coords state))))

(defn add-mode [state mode-id]
  "Adds a new mode to the state"
  (if (mode? state mode-id)
    @state
    (swap! state assoc-in [:modes mode-id] (empty-mode))))

(defn active-side [state] ((active-mode state) :side))
(defn active-side-page [state] (side/get-page (active-side state) (grid-y-page state)))
(defn active-grid [state] ((active-mode state) :grid))
(defn active-page [state] (grid/get-page (page-coords state) (active-grid state)))

(defn x-max        [state] (grid/x-max (active-grid state)))
(defn y-max        [state] (grid/y-max (active-grid state)))

(defn on?
  ([state column row] (on? state column row (page-coords state)))
  ([state column row page-coords] (grid/on? page-coords (active-grid state) column row)))

(defn visible? [state column row]
  (let [[x-page y-page] (page-coords state)]
    (and (= x-page (int (/ column grid/grid-width)))
         (= y-page (int (/ row grid/grid-height))))))

(defn- set-session-mode [state mode value]
  (swap! state assoc-in [:modes mode :session?] value))

(defn disable-session-mode!
  ([state] (disable-session-mode! state (mode state)))
  ([state mode]
   (set-session-mode state mode false)))

(defn enable-session-mode!
  ([state] (enable-session-mode! state (mode state)))
  ([state mode]
   (set-session-mode state mode true)))

(defn session-mode?
  ([state] (session-mode? state (mode state)))
  ([state mode] (get-in @state [:modes mode :session?])))


(defn handle-key
  ([state mode-id] (handle-key state mode-id (page-coords state)))
  ([state mode-id [column row]] (keyword (subs (str mode-id "-" column "x" row) 1))))

(defn- active-handle-key [state] (handle-key state (mode state)))
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

(defn- shift-page-right* [state current-mode next-x-page y-page x-page-count]
  (when (>= next-x-page x-page-count)
    (swap! state assoc-in [:modes current-mode :grid] (grid/add-page-left (active-grid state))))
  (swap! state assoc :page-coords [next-x-page y-page]))

(defn shift-page-right [state]
  (let [current-mode (mode state)
        [x-page y-page] (page-coords state)
        x-page-count (grid/x-page-count (active-grid state))
        next-x-page (inc x-page)]

    (if-let [page-max-x (get-in @state [:modes current-mode :page-max-x])]
      (if (<= next-x-page page-max-x)
        (shift-page-right* state current-mode next-x-page y-page x-page-count)
        @state)

      (shift-page-right* state current-mode next-x-page y-page x-page-count))))

(defn shift-page-up [state]
  (let [y-page (grid-y-page state)]
    (if (> y-page 0)
      (set-page-y state (dec y-page))
      @state)))

(defn- shift-page-down* [state current-mode x-page next-y-page y-page-count]
  (when (>= next-y-page y-page-count)
    (swap! state assoc-in [:modes current-mode :grid] (grid/add-page-below (active-grid state)))
    (swap! state assoc-in [:modes current-mode :side] (side/add-page-below (active-side state))))
  (swap! state assoc :page-coords [x-page next-y-page]))

(defn shift-page-down [state]
  (let [current-mode (mode state)
        [x-page y-page] (page-coords state)
        y-page-count (grid/y-page-count (active-grid state))
        next-y-page (inc y-page)]

    (if-let [page-max-y (get-in @state [:modes current-mode :page-max-y])]
      (if (<= next-y-page page-max-y)
        (shift-page-down* state current-mode x-page next-y-page y-page-count)
        @state)

      (shift-page-down* state current-mode x-page next-y-page y-page-count))))

(defn set-page-max-x
  ([state value] (set-page-max-x state (mode state) value))
  ([state mode value] (swap! state assoc-in [:modes mode :page-max-x] value)))

(defn set-page-max-y
  ([state value] (set-page-max-y state (mode state) value))
  ([state mode value] (swap! state assoc-in [:modes mode :page-max-y] value)))

(defn reset-state! [state] (reset! state (empty-state-map)))

(defn complete-row [state row]
  "Return a single row spanning all dimensions"
  (grid/complete-row (active-grid state) row))

(defn write-complete-grid-row!
  [state row row-data]
  (let [new-grid (grid/write-complete-grid-row (active-grid state) row row-data)]
    (swap! state assoc-in [:modes (mode state) :grid] new-grid)))


(comment
(def state (atom {:modes {:default {:grid [[0 1 0 0 0 0 0 0] [0 1 0 0 0 0 0 0] [0 1 0 0 0 0 0 0] [0 1 0 0 0 0 0 0]] :side [[1 1][0 0]]}}, :active :default, :page-coords [0 0]}))
)
