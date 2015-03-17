(ns launchkey-mini.side)

(def side-btn-height 2)

(defn- empty-rows []
  "Initializes a new set of empty side rows"
  (vec (map (fn [x] 0) (range 0 side-btn-height))))

(defn empty-side []
  "Initializes a list of empty side states"
  [(empty-rows)])

(defn cell [side row page]
  "Gets the state of a row on the side"
  (get-in side [page row] 0))

(defn absolute-cell [side page]
  "Gets the state of the absolute side cell by specifying the page"
  (cell side (mod page side-btn-height) (int (/ page side-btn-height))))

(defn on? [side row page]
  "checks, if the specified side cell is on"
  (not= 0 (cell side row page)))

(defn absolute-on? [side page]
  "checks, if the specified side cell is on (specified by the page)"
  (not= 0 (absolute-cell side page)))

(defn toggle [side row page]
  "Toggles the state of the given cell"
  (let [new-cell (if (on? side row page) 0 1)]
    (assoc-in side [page row] new-cell)))

(defn add-page-below [side]
  "Adds a new pair of"
  (conj side (empty-rows)))

(defn get-page [side-data page-number]
  "Gets the specified page"
  (nth side-data page-number))
