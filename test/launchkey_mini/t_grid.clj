(ns launchkey-mini.t-grid
  (:require [midje.sweet :refer :all]
            [launchkey-mini.grid :as grid]))

(fact "coordinate->note returns correct note"
  (#'grid/coordinate->note 0 0) => 0x60
  (#'grid/coordinate->note 0 7) => 0x67
  (#'grid/coordinate->note 1 0) => 0x70
  (#'grid/coordinate->note 1 7) => 0x77)

(fact "coordinate->note wraps at x-overflow"
  (#'grid/coordinate->note 0 8) => 0x60
  (#'grid/coordinate->note 1 8) => 0x70)

(fact "coordinate->note wraps at y-overflow"
  (#'grid/coordinate->note 2 0) => 0x60)

(fact "x-page-count returns correct count"
  (#'grid/x-page-count (#'grid/virtual-grid 1 2)) => 2
  (#'grid/x-page-count (#'grid/virtual-grid 2)) => 1
  (#'grid/x-page-count (#'grid/empty-grid)) => 1)

(fact "y-page-count returns correct count"
  (#'grid/y-page-count (#'grid/virtual-grid 1 2)) => 1
  (#'grid/y-page-count (#'grid/virtual-grid 2)) => 2
  (#'grid/y-page-count (#'grid/empty-grid)) => 1)

(fact "x-offset returns correct offset"
  (#'grid/x-offset 0 0) => 0
  (#'grid/x-offset 0 1) => 8
  (#'grid/x-offset 0 2) => 16
  (#'grid/x-offset 1 0) => 1
  (#'grid/x-offset 1 1) => 9
  (#'grid/x-offset 1 2) => 17)

(fact "y-offset returns correct offset"
  (#'grid/y-offset 0 0) => 0
  (#'grid/y-offset 0 1) => 2
  (#'grid/y-offset 0 2) => 4
  (#'grid/y-offset 1 0) => 1
  (#'grid/y-offset 1 1) => 3
  (#'grid/y-offset 1 2) => 5)

(fact "x-max returns grid total x size"
  (#'grid/x-max (#'grid/empty-grid)) => 8
  (#'grid/x-max (#'grid/virtual-grid 1 2)) => 16)

(fact "y-max returns grid total y size"
  (#'grid/y-max (#'grid/empty-grid)) => 2
  (#'grid/y-max (#'grid/virtual-grid 2 1)) => 4)

(fact "toggle toggles correct cell with default grid"
  (#'grid/toggle (#'grid/empty-grid) 0 0) => [[1 0 0 0 0 0 0 0]
                                              [0 0 0 0 0 0 0 0]]
  (#'grid/toggle (#'grid/empty-grid) 1 5) => [[0 0 0 0 0 0 0 0]
                                              [0 0 0 0 0 1 0 0]])

(fact "toggle toggles correct cell with extended grid"
  (#'grid/toggle [1 0] (#'grid/virtual-grid 2 1) 0 0) => [[0 0 0 0 0 0 0 0]
                                                          [0 0 0 0 0 0 0 0]
                                                          [1 0 0 0 0 0 0 0]
                                                          [0 0 0 0 0 0 0 0]]
  (#'grid/toggle [0 1] (#'grid/virtual-grid 1 2) 0 0) => [[0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0]
                                                          [0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0]])

(fact "set-cell sets given value to correct cell with default grid"
  (#'grid/set-cell (#'grid/empty-grid) 0 0 5) => [[5 0 0 0 0 0 0 0]
                                                  [0 0 0 0 0 0 0 0]]
  (#'grid/set-cell (#'grid/empty-grid) 1 5 4) => [[0 0 0 0 0 0 0 0]
                                                  [0 0 0 0 0 4 0 0]])

(fact "set-cell sets given value to correct cell with extended grid"
  (#'grid/set-cell [1 0] (#'grid/virtual-grid 2 1) 0 0 5) => [[0 0 0 0 0 0 0 0]
                                                              [0 0 0 0 0 0 0 0]
                                                              [5 0 0 0 0 0 0 0]
                                                              [0 0 0 0 0 0 0 0]]
  (#'grid/set-cell [0 1] (#'grid/virtual-grid 1 2) 0 0 3) => [[0 0 0 0 0 0 0 0 3 0 0 0 0 0 0 0]
                                                              [0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0]])

(fact "cell returns the correct value for a default grid"
  (#'grid/cell [[1 2 3 4 5 6 7 8][0 0 0 0 0 0 0 0]] 0 3) => 4
  (#'grid/cell [[0 0 0 0 0 0 0 0][1 2 3 4 5 6 7 8]] 1 5) => 6)

(fact "cell returns the correct value for an extended grid"
  (#'grid/cell [1 1]
    [[0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0]
     [0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0]
     [0 0 0 0 0 0 0 0 0 0 0 0xB 0 0 0 0]
     [0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0]] 0 3) => 0xB)

(fact "absolute-cell returns the correct value for an extended grid"
  (#'grid/absolute-cell
    [[0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0]
     [0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0]
     [0 0 0 0 0 0 0 0 0 0 0 0xB 0 0 0 0]
     [0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0]] 2 11) => 0xB)

(def multi-page-grid [[0 0 0 0 0 0 0 0 1 1 1 1 1 1 1 1]
                      [0 0 0 0 0 0 0 0 1 1 1 1 1 1 1 1]
                      [2 2 2 2 2 2 2 2 3 3 3 3 3 3 3 3]
                      [2 2 2 2 2 2 2 2 3 3 3 3 3 3 3 3]])

(facts "Test get-page"
  (#'grid/get-page multi-page-grid) => [[0 0 0 0 0 0 0 0]
                                        [0 0 0 0 0 0 0 0]]
  (#'grid/get-page [0 0] multi-page-grid) => [[0 0 0 0 0 0 0 0]
                                              [0 0 0 0 0 0 0 0]]
  (#'grid/get-page [0 1] multi-page-grid) => [[1 1 1 1 1 1 1 1]
                                              [1 1 1 1 1 1 1 1]]
  (#'grid/get-page [1 0] multi-page-grid) => [[2 2 2 2 2 2 2 2]
                                              [2 2 2 2 2 2 2 2]]
  (#'grid/get-page [1 1] multi-page-grid) => [[3 3 3 3 3 3 3 3]
                                              [3 3 3 3 3 3 3 3]]
  (#'grid/get-page [0 2] multi-page-grid) => [[0 0 0 0 0 0 0 0]
                                              [0 0 0 0 0 0 0 0]]
  (#'grid/get-page [2 0] multi-page-grid) => [[0 0 0 0 0 0 0 0]
                                              [0 0 0 0 0 0 0 0]]
  (#'grid/get-page [2 2] multi-page-grid) => [[0 0 0 0 0 0 0 0]
                                              [0 0 0 0 0 0 0 0]])
