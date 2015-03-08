(ns launchkey-mini.grid)

(def grid-width  8)
(def grid-height 2)

(def grid-notes
  [(range 0x60 0x68)
  (range 0x70 0x78)])

(defn coordinate->note [row col]
  (-> grid-notes (nth (mod row grid-height)) (nth (mod col grid-width))))
