(ns launchkey-mini.grid)

(defn fn-grid [] {})

(def grid-width  8)
(def grid-height 2)

(def grid-notes
  "Notes needed for controlling the LEDs"
  [(range 0x60 (+ 0x60 grid-width))
  (range 0x70 (+ 0x70 grid-width))])

(defn coordinate->note [row col]
  "Converts a zero based coordinate (row, column) into a not for
  controlling the LED of the note.
  (wraps around at overflow)"
  (-> grid-notes (nth (mod row grid-height)) (nth (mod col grid-width))))

(defn- empty-row
  "creates an empty row of the given width (by default grid-width)"
  ([] (empty-row grid-width))
  ([column-count] (vec (take column-count (repeat 0)))))

(defn virtual-grid
  "creates a virtual grid of multiple pages in two dimensions"
  ([y-pages] (virtual-grid y-pages 1))
  ([y-pages x-pages]
    (let [overall-columns (* grid-width x-pages)
          overall-rows (* grid-height y-pages)]
      (vec (take overall-rows (repeat (empty-row overall-columns)))))))

(defn empty-grid []
  "creates the default empty grid"
  (virtual-grid 1))

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
  ([grid row column] (toggle [0 0] grid row column))
  ([[y-page x-page] grid row column]
     (let [x-offset (x-offset column x-page)
           y-offset (y-offset row y-page)
           old-row (-> grid (nth y-offset) (vec))
           old-cell (nth old-row x-offset)
           new-row (assoc old-row x-offset (if (= 1 old-cell) 0 1))
           new-grid (assoc (vec grid) y-offset new-row)]
       new-grid)))

(defn set-cell
  "Set the content of a cell within a grid to a given value"
  ([grid row column value] (set-cell [0 0] grid row column value))
  ([[y-page x-page] grid row column value]
     (let [y-offset (y-offset row y-page)
           old-row (-> grid (nth y-offset) (vec))
           new-row (assoc old-row (x-offset column x-page) value)]
        (assoc (vec grid) y-offset new-row))))

(defn cell
  "Get the value of a cell"
  ([grid row column] (cell [0 0] grid row column))
  ([[y-page x-page] grid row column]
     (let [x-offset (x-offset column x-page)
           y-offset (y-offset row y-page)]
       (-> grid
           (nth y-offset)
           (nth x-offset)))))

(defn absolute-cell [grid row column]
  "Get the value of a cell by its absolute coordinates"
  (if (and (< row (y-max grid)) (< column (x-max grid)))
    (-> grid (nth row) (nth column))
    0))

(defn get-page
  "Gets the specified page"
  ([full-grid] (get-page [0 0] full-grid))
  ([[y-page x-page] full-grid]
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
  ([grid row column] (on? [0 0] grid row column))
  ([page-coordinates grid row column] (not= 0 (cell page-coordinates grid row column))))

(defn get-row
  ([grid row] (get-row [0 0] grid row))
  ([[x-page y-page] grid row]
    (let [y-offset (y-offset (mod row grid-height) y-page)]
      (->>
        (nth grid y-offset)
        (drop (x-offset 0 x-page))
        (take (x-offset grid-width x-page))))))
