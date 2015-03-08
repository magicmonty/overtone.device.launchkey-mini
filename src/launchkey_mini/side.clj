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
  [side row virtual-row]
  (get-in side [virtual-row row] 0))

(defn absolute-cell
  "Gets the state of the absolute side cell by specifying the virtual row"
  [side virtual-row]
  (cell side (mod virtual-row side-btn-height) (int (/ virtual-row side-btn-height))))

(defn on?
  "checks, if the specified side cell is on"
  [side row virtual-row]
  (not= 0 (cell side row virtual-row)))

(defn absolute-on?
  "checks, if the specified side cell is on (specified by the virtual row)"
  [side virtual-row]
  (not= 0 (absolute-cell side virtual-row)))

(defn toggle
  "Toggles the state of the given cell"
  [side row virtual-row]
  (let [new-cell (if (on? side row virtual-row) 0 1)]
    (assoc-in side [virtual-row row] new-cell)))

(defn shift-down
  "Adds a new pair of"
 [side]
  (conj side (empty-rows)))

(defn project
  "Gets the projection of the rows"
  [side virtual-row]
  (nth side virtual-row))
