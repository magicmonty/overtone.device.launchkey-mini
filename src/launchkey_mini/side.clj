(ns launchkey-mini.side)

(def side-btn-height 2)

(defn- empty-rows
  "Initializes a new set of empty side rows"
  []
  (vec (map (fn [x] 0) (range 0 side-btn-height))))

(defn empty-side
  "Initializes a list of empty side states"
  []
  [(empty-rows)])

(defn cell
  "Gets the state of a row on the side"
  [side row page]
  (get-in side [page row] 0))

(defn absolute-cell
  "Gets the state of the absolute side cell by specifying the page"
  [side page]
  (cell side (mod page side-btn-height) (int (/ page side-btn-height))))

(defn on?
  "checks, if the specified side cell is on"
  [side row page]
  (not= 0 (cell side row page)))

(defn absolute-on?
  "checks, if the specified side cell is on (specified by the page)"
  [side page]
  (not= 0 (absolute-cell side page)))

(defn toggle
  "Toggles the state of the given cell"
  [side row page]
  (let [new-cell (if (on? side row page) 0 1)]
    (assoc-in side [page row] new-cell)))

(defn shift-down
  "Adds a new pair of"
 [side]
  (conj side (empty-rows)))

(defn get-page
  "Gets the specified page"
  [side page]
  (nth side page))
