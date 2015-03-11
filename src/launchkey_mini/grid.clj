(ns launchkey-mini.grid
  (:use [slingshot.slingshot :only [throw+]]))

(defn fn-grid [] {})

(def grid-width  8)
(def grid-height 2)

(def grid-notes
  "Notes needed for controlling the LEDs"
  [(range 0x60 (+ 0x60 grid-width))
  (range 0x70 (+ 0x70 grid-width))])

(defn coordinate->note [col row]
  "Converts a zero based coordinate (column, row) into a
  midi note for controlling the LED of the grid pad.
  (wraps around at overflow)"
  (-> grid-notes (nth (mod row grid-height)) (nth (mod col grid-width))))

(defn- empty-row
  "creates an empty row of the given width (by default grid-width)"
  ([] (empty-row grid-width))
  ([column-count] (vec (take column-count (repeat 0)))))

(defn virtual-grid
  "creates a virtual grid of multiple pages in two dimensions"
  ([] (virtual-grid 1))
  ([y-pages] (virtual-grid 1 y-pages))
  ([x-pages y-pages]
    (let [overall-columns (* grid-width x-pages)
          overall-rows (* grid-height y-pages)]
      (vec (take overall-rows (repeat (empty-row overall-columns)))))))

(defn empty-grid []
  "creates the default empty grid"
  (virtual-grid))

(defn x-offset
  "Calculates the x-offset of a pad within a full virtual grid"
  [x x-page] (+ x (* x-page grid-width)))

(defn y-offset
  "Calculates the x-offset of a pad within a full virtual grid"
  [y y-page] (+ y (* y-page grid-height)))

(defn x-page-count [grid]
  "Gets the count of horizontal pages"
  (int (/ (count (first grid)) grid-width)))

(defn y-page-count [grid]
  "Gets the count of vertical pages"
  (int (/ (count grid) grid-height)))

(defn x-max [grid]
  "Gets the overall column count"
  (count (first grid)))

(defn y-max [grid]
  "Gets the overall row count"
  (count grid))

(defn toggle
  "Toggles a cell in a row on or off"
  ([grid column row] (toggle [0 0] grid column row))
  ([[x-page y-page] grid column row]
     (let [x-offset (x-offset column x-page)
           y-offset (y-offset row y-page)
           old-row (-> grid (nth y-offset) (vec))
           old-cell (nth old-row x-offset)
           new-row (assoc old-row x-offset (if (= 1 old-cell) 0 1))
           new-grid (assoc (vec grid) y-offset new-row)]
       new-grid)))

(defn set-cell
  "Set the content of a cell within a grid to a given value"
  ([grid column row value] (set-cell [0 0] grid column row value))
  ([[x-page y-page] grid column row value]
     (let [y-offset (y-offset row y-page)
           old-row (-> grid (nth y-offset) (vec))
           new-row (assoc old-row (x-offset column x-page) value)]
        (assoc (vec grid) y-offset new-row))))

(defn cell
  "Get the value of a cell"
  ([grid column row] (cell [0 0] grid column row))
  ([[x-page y-page] grid column row]
     (let [x-offset (x-offset column x-page)
           y-offset (y-offset row y-page)]
       (-> grid
           (nth y-offset)
           (nth x-offset)))))

(defn absolute-cell [grid column row]
  "Get the value of a cell by its absolute coordinates"
  (if (and (< row (y-max grid)) (< column (x-max grid)))
    (-> grid (nth row) (nth column))
    0))

(defn get-page
  "Gets the specified page"
  ([full-grid] (get-page [0 0] full-grid))
  ([[x-page y-page] full-grid]
     (map (fn [row]
            (let [new-row (->> row
                               (drop (x-offset 0 x-page))
                               (take grid-width)
                               seq)]
              (or new-row (take grid-width (repeat 0)))))
          (let [rest (drop (y-offset 0 y-page) full-grid)]
            (take grid-height (if (empty? rest)
                                  (repeat (empty-row (x-max full-grid)))
                                  rest))))))

(defn on?
  "Checks, if a cell is on or off"
  ([grid column row] (on? [0 0] grid column row))
  ([page-coordinates grid column row] (not= 0 (cell page-coordinates grid column row))))

(defn get-row
  "Gets a row by its row number (0 or 1, wraps around) and page coordinates"
  ([grid row] (get-row [0 0] grid row))
  ([[x-page y-page] grid row]
    (let [y-offset (y-offset (mod row grid-height) y-page)]
      (->>
        (nth grid y-offset)
        (drop (x-offset 0 x-page))
        (take (x-offset grid-width x-page))))))

(defn get-column
  "Gets a column by its column number (0 to 7, wraps around) and page coordinates"
  ([grid column] (get-column [0 0] grid column))
  ([[x-page y-page] grid column]
    (let [x-offset (x-offset (mod column grid-width) x-page)]
      (if (< x-offset (x-max grid))
        (map #(nth % x-offset) (take grid-height (drop (y-offset 0 y-page) grid)))
        (take grid-height (repeat 0))))))

(defn absolute-column
  "Direct access into the grid irrelevant of x grid-index"
  [[_ y-page] grid column]
  (let [grid-column (cond
                      (> column grid-width) (mod (int (dec (+ (/ column grid-width) column))) (+ 1 (x-max grid)))
                      true                  column)]
    (if (< grid-column (x-max grid))
      (map #(nth % grid-column) (take grid-height (drop (y-offset 0 y-page) grid)))
      (take grid-height (repeat 0)))))

(defn complete-row [grid row]
  "Direct access into the entire grid, ignores any grid position"
  (nth grid (mod row (count grid))))

(defn add-page-left [grid]
  "adds a new page on the left side of the grid"
  (map #(concat % (take grid-width (repeat 0))) grid))

(defn add-page-bottom [grid]
  "adds a new page on the bottom of the grid"
  (let [x (x-page-count  grid)]
    (concat grid (repeat grid-height (empty-row (* x grid-width))))))

(defn write-complete-grid-row [grid y row-data]
  (when-not (= (count row-data) (x-max grid))
    (throw+ {:type ::DifferingRowSize :hint (str "row-data must match grid row size. The grid has rows: " (x-max grid) " passed row-data has: " (count row-data))}))
  (assoc (vec grid) (mod y (y-max grid)) (map int row-data)))

