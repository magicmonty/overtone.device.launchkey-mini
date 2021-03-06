(ns launchkey-mini.t-grid
  (:require [midje.sweet :refer :all]
            [launchkey-mini.grid :as grid]))

(fact "coordinate->note returns correct note"
  (#'grid/coordinate->note 0 0) => 0x60
  (#'grid/coordinate->note 7 0) => 0x67
  (#'grid/coordinate->note 0 1) => 0x70
  (#'grid/coordinate->note 7 1) => 0x77)

(fact "coordinate->note wraps at x-overflow"
  (#'grid/coordinate->note 8 0) => 0x60
  (#'grid/coordinate->note 8 1) => 0x70)

(fact "coordinate->note wraps at y-overflow"
  (#'grid/coordinate->note 0 2) => 0x60)

(fact "x-page-count returns correct count"
  (#'grid/x-page-count (#'grid/virtual-grid 2 1)) => 2
  (#'grid/x-page-count (#'grid/virtual-grid 2)) => 1
  (#'grid/x-page-count (#'grid/empty-grid)) => 1)

(fact "y-page-count returns correct count"
  (#'grid/y-page-count (#'grid/virtual-grid 2 1)) => 1
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
  (#'grid/y-offset 1 0) => 1
  (#'grid/y-offset 0 1) => 2
  (#'grid/y-offset 1 1) => 3
  (#'grid/y-offset 0 2) => 4
  (#'grid/y-offset 1 2) => 5)

(fact "x-max returns grid total x size"
  (#'grid/x-max (#'grid/empty-grid)) => 8
  (#'grid/x-max (#'grid/virtual-grid 2 1)) => 16)

(fact "y-max returns grid total y size"
  (#'grid/y-max (#'grid/empty-grid)) => 2
  (#'grid/y-max (#'grid/virtual-grid 1 2)) => 4)

(fact "toggle toggles correct cell with default grid"
  (#'grid/toggle (#'grid/empty-grid) 0 0) => [[1 0 0 0 0 0 0 0]
                                              [0 0 0 0 0 0 0 0]]
  (#'grid/toggle (#'grid/empty-grid) 5 1) => [[0 0 0 0 0 0 0 0]
                                              [0 0 0 0 0 1 0 0]])

(fact "toggle toggles correct cell with extended grid"
  (#'grid/toggle [0 1] (#'grid/virtual-grid 1 2) 0 0) => [[0 0 0 0 0 0 0 0]
                                                          [0 0 0 0 0 0 0 0]
                                                          [1 0 0 0 0 0 0 0]
                                                          [0 0 0 0 0 0 0 0]]
  (#'grid/toggle [1 0] (#'grid/virtual-grid 2 1) 0 0) => [[0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0]
                                                          [0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0]])

(fact "set-cell sets given value to correct cell with default grid"
  (#'grid/set-cell (#'grid/empty-grid) 0 0 5) => [[5 0 0 0 0 0 0 0]
                                                  [0 0 0 0 0 0 0 0]]
  (#'grid/set-cell (#'grid/empty-grid) 5 1 4) => [[0 0 0 0 0 0 0 0]
                                                  [0 0 0 0 0 4 0 0]])

(fact "set-cell sets given value to correct cell with extended grid"
  (#'grid/set-cell [0 1] (#'grid/virtual-grid 1 2) 0 0 5) => [[0 0 0 0 0 0 0 0]
                                                              [0 0 0 0 0 0 0 0]
                                                              [5 0 0 0 0 0 0 0]
                                                              [0 0 0 0 0 0 0 0]]
  (#'grid/set-cell [1 0] (#'grid/virtual-grid 2 1) 0 0 3) => [[0 0 0 0 0 0 0 0 3 0 0 0 0 0 0 0]
                                                              [0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0]])

(fact "cell returns the correct value for a default grid"
  (#'grid/cell [[1 2 3 4 5 6 7 8][0 0 0 0 0 0 0 0]] 3 0) => 4
  (#'grid/cell [[0 0 0 0 0 0 0 0][1 2 3 4 5 6 7 8]] 5 1) => 6)

(fact "cell returns the correct value for an extended grid"
  (#'grid/cell [1 1]
    [[0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0]
     [0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0]
     [0 0 0 0 0 0 0 0 0 0 0 0xB 0 0 0 0]
     [0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0]] 3 0) => 0xB)

(fact "absolute-cell returns the correct value for an extended grid"
  (#'grid/absolute-cell
    [[0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0]
     [0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0]
     [0 0 0 0 0 0 0 0 0 0 0 0xB 0 0 0 0]
     [0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0]] 11 2) => 0xB)

(def multi-page-grid [[0 0 0 0 0 0 0 0 1 1 1 1 1 1 1 1]
                      [0 0 0 0 0 0 0 0 1 1 1 1 1 1 1 1]
                      [2 2 2 2 2 2 2 2 3 3 3 3 3 3 3 3]
                      [2 2 2 2 2 2 2 2 3 3 3 3 3 3 3 3]])

(facts "Test get-page"
  (#'grid/get-page multi-page-grid) => [[0 0 0 0 0 0 0 0]
                                        [0 0 0 0 0 0 0 0]]
  (#'grid/get-page [0 0] multi-page-grid) => [[0 0 0 0 0 0 0 0]
                                              [0 0 0 0 0 0 0 0]]
  (#'grid/get-page [1 0] multi-page-grid) => [[1 1 1 1 1 1 1 1]
                                              [1 1 1 1 1 1 1 1]]
  (#'grid/get-page [0 1] multi-page-grid) => [[2 2 2 2 2 2 2 2]
                                              [2 2 2 2 2 2 2 2]]
  (#'grid/get-page [1 1] multi-page-grid) => [[3 3 3 3 3 3 3 3]
                                              [3 3 3 3 3 3 3 3]]
  (#'grid/get-page [2 0] multi-page-grid) => [[0 0 0 0 0 0 0 0]
                                              [0 0 0 0 0 0 0 0]]
  (#'grid/get-page [0 2] multi-page-grid) => [[0 0 0 0 0 0 0 0]
                                              [0 0 0 0 0 0 0 0]]
  (#'grid/get-page [2 2] multi-page-grid) => [[0 0 0 0 0 0 0 0]
                                              [0 0 0 0 0 0 0 0]])

(def multi-page-grid [[0 1 0 1 0 1 0 1 2 2 2 2 2 2 2 2]
                      [1 0 1 0 1 0 1 0 2 2 2 2 2 2 2 2]
                      [3 3 3 3 3 3 3 3 0 0 0 0 0 0 0 0]
                      [3 3 3 3 3 3 3 3 0 0 0 0 0 0 0 0]])

(fact "on? gets correct values for simple call"
  (#'grid/on? multi-page-grid 0 0) => false
  (#'grid/on? multi-page-grid 1 0) => true
  (#'grid/on? multi-page-grid 0 1) => true
  (#'grid/on? multi-page-grid 1 1) => false)

(fact "on? gets correct values for multi-page-grid call"
  (#'grid/on? [0 0] multi-page-grid 0 0) => false
  (#'grid/on? [0 0] multi-page-grid 1 0) => true
  (#'grid/on? [0 0] multi-page-grid 0 1) => true
  (#'grid/on? [0 0] multi-page-grid 1 1) => false
  (#'grid/on? [0 1] multi-page-grid 1 0) => true
  (#'grid/on? [1 0] multi-page-grid 0 0) => true
  (#'grid/on? [1 1] multi-page-grid 0 1) => false)

(def multi-page-grid [[0 1 0 1 0 1 0 1 0 2 0 2 0 2 0 2]
                      [1 0 1 0 1 0 1 0 2 0 2 0 2 0 2 0]
                      [0 3 0 3 0 3 0 3 0 4 0 4 0 4 0 4]
                      [3 0 3 0 3 0 3 0 4 0 4 0 4 0 4 0]])

(fact "get-row returns a single row"
  (#'grid/get-row multi-page-grid 0) => [0 1 0 1 0 1 0 1]
  (#'grid/get-row multi-page-grid 1) => [1 0 1 0 1 0 1 0])

(fact "get-row returns a single row in multi-page-grid"
  (#'grid/get-row [0 0] multi-page-grid 0) => [0 1 0 1 0 1 0 1]
  (#'grid/get-row [0 0] multi-page-grid 1) => [1 0 1 0 1 0 1 0]
  (#'grid/get-row [1 0] multi-page-grid 0) => [0 2 0 2 0 2 0 2]
  (#'grid/get-row [1 0] multi-page-grid 1) => [2 0 2 0 2 0 2 0]
  (#'grid/get-row [0 1] multi-page-grid 0) => [0 3 0 3 0 3 0 3]
  (#'grid/get-row [0 1] multi-page-grid 1) => [3 0 3 0 3 0 3 0]
  (#'grid/get-row [1 1] multi-page-grid 0) => [0 4 0 4 0 4 0 4]
  (#'grid/get-row [1 1] multi-page-grid 1) => [4 0 4 0 4 0 4 0])

(fact "get-row wraps around"
  (#'grid/get-row multi-page-grid 2) => [0 1 0 1 0 1 0 1]
  (#'grid/get-row multi-page-grid 3) => [1 0 1 0 1 0 1 0]
  (#'grid/get-row [0 0] multi-page-grid 2) => [0 1 0 1 0 1 0 1]
  (#'grid/get-row [0 0] multi-page-grid 3) => [1 0 1 0 1 0 1 0]
  (#'grid/get-row [1 0] multi-page-grid 2) => [0 2 0 2 0 2 0 2]
  (#'grid/get-row [1 0] multi-page-grid 3) => [2 0 2 0 2 0 2 0]
  (#'grid/get-row [0 1] multi-page-grid 2) => [0 3 0 3 0 3 0 3]
  (#'grid/get-row [0 1] multi-page-grid 3) => [3 0 3 0 3 0 3 0]
  (#'grid/get-row [1 1] multi-page-grid 2) => [0 4 0 4 0 4 0 4]
  (#'grid/get-row [1 1] multi-page-grid 3) => [4 0 4 0 4 0 4 0])

(def multi-page-grid [[0 1 0 0 0 0 0 5 0 2 0 0 0 0 0 6]
                      [1 0 0 0 0 0 0 5 2 0 0 0 0 0 0 6]
                      [0 3 0 0 0 0 0 7 0 4 0 0 0 0 0 8]
                      [3 0 0 0 0 0 0 7 4 0 0 0 0 0 0 8]])

(fact "get-column returns a single column"
  (#'grid/get-column multi-page-grid 0) => [0 1]
  (#'grid/get-column multi-page-grid 1) => [1 0]
  (#'grid/get-column multi-page-grid 7) => [5 5])

(fact "get-column returns a single column in a multi-page-grid"
  (#'grid/get-column [0 0] multi-page-grid 0) => [0 1]
  (#'grid/get-column [0 0] multi-page-grid 1) => [1 0]
  (#'grid/get-column [0 0] multi-page-grid 7) => [5 5]
  (#'grid/get-column [1 0] multi-page-grid 0) => [0 2]
  (#'grid/get-column [1 0] multi-page-grid 1) => [2 0]
  (#'grid/get-column [1 0] multi-page-grid 7) => [6 6]
  (#'grid/get-column [0 1] multi-page-grid 0) => [0 3]
  (#'grid/get-column [0 1] multi-page-grid 1) => [3 0]
  (#'grid/get-column [0 1] multi-page-grid 7) => [7 7]
  (#'grid/get-column [1 1] multi-page-grid 0) => [0 4]
  (#'grid/get-column [1 1] multi-page-grid 1) => [4 0]
  (#'grid/get-column [1 1] multi-page-grid 7) => [8 8])

(fact "get-column wraps around"
  (#'grid/get-column multi-page-grid 8) => [0 1]
  (#'grid/get-column [0 0] multi-page-grid 8) => [0 1]
  (#'grid/get-column [0 1] multi-page-grid 8) => [0 3]
  (#'grid/get-column [1 0] multi-page-grid 8) => [0 2]
  (#'grid/get-column [1 1] multi-page-grid 8) => [0 4])

(fact "absolute-column returns a complete column"
  (#'grid/absolute-column [0 0] multi-page-grid 0) => [0 1]
  (#'grid/absolute-column [0 1] multi-page-grid 0) => [0 3]
  (#'grid/absolute-column [0 0] multi-page-grid 7) => [5 5]
  (#'grid/absolute-column [0 1] multi-page-grid 7) => [7 7]
  (#'grid/absolute-column [0 0] multi-page-grid 9) => [2 0]
  (#'grid/absolute-column [0 1] multi-page-grid 9) => [4 0]
  (#'grid/absolute-column [0 0] multi-page-grid 15) => [6 6]
  (#'grid/absolute-column [0 1] multi-page-grid 15) => [8 8])

(fact "absolute-column wraps around"
  (#'grid/absolute-column [0 0] multi-page-grid 16) => [0 1]
  (#'grid/absolute-column [0 1] multi-page-grid 16) => [0 3]
  (#'grid/absolute-column [1 0] multi-page-grid 16) => [0 1]
  (#'grid/absolute-column [1 1] multi-page-grid 16) => [0 3])

(fact "complete-row gets complete row"
  (#'grid/complete-row multi-page-grid 0) => [0 1 0 0 0 0 0 5 0 2 0 0 0 0 0 6]
  (#'grid/complete-row multi-page-grid 1) => [1 0 0 0 0 0 0 5 2 0 0 0 0 0 0 6]
  (#'grid/complete-row multi-page-grid 2) => [0 3 0 0 0 0 0 7 0 4 0 0 0 0 0 8]
  (#'grid/complete-row multi-page-grid 3) => [3 0 0 0 0 0 0 7 4 0 0 0 0 0 0 8])

(fact "complete-row wraps around"
  (#'grid/complete-row multi-page-grid 4) => [0 1 0 0 0 0 0 5 0 2 0 0 0 0 0 6]
  (#'grid/complete-row multi-page-grid 5) => [1 0 0 0 0 0 0 5 2 0 0 0 0 0 0 6]
  (#'grid/complete-row multi-page-grid 6) => [0 3 0 0 0 0 0 7 0 4 0 0 0 0 0 8]
  (#'grid/complete-row multi-page-grid 7) => [3 0 0 0 0 0 0 7 4 0 0 0 0 0 0 8])

(def multi-page-grid [[1 0 0 0 0 0 0 1]
                      [1 0 0 0 0 0 0 1]
                      [2 0 0 0 0 0 0 2]
                      [2 0 0 0 0 0 0 2]])

(fact "add-page-left adds page to all rows"
  (#'grid/add-page-left multi-page-grid) => [[1 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0]
                                             [1 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0]
                                             [2 0 0 0 0 0 0 2 0 0 0 0 0 0 0 0]
                                             [2 0 0 0 0 0 0 2 0 0 0 0 0 0 0 0]])

(fact "add-page-below adds two new rows"
      (#'grid/add-page-below multi-page-grid) => [[1 0 0 0 0 0 0 1]
                                                  [1 0 0 0 0 0 0 1]
                                                  [2 0 0 0 0 0 0 2]
                                                  [2 0 0 0 0 0 0 2]
                                                  [0 0 0 0 0 0 0 0]
                                                  [0 0 0 0 0 0 0 0]])

(def multi-page-grid [[1 0 0 0 0 0 0 1]
                      [2 0 0 0 0 0 0 2]])

(fact "write-complete-grid-row replaces given row"
  (#'grid/write-complete-grid-row multi-page-grid 0 [1 2 3 4 5 6 7 8])
    => [[1 2 3 4 5 6 7 8]
        [2 0 0 0 0 0 0 2]]
  (#'grid/write-complete-grid-row multi-page-grid 1 [1 2 3 4 5 6 7 8])
    => [[1 0 0 0 0 0 0 1]
        [1 2 3 4 5 6 7 8]])

(fact "write-complete-grid-row wraps around"
  (#'grid/write-complete-grid-row multi-page-grid 2 [1 2 3 4 5 6 7 8])
    => [[1 2 3 4 5 6 7 8]
        [2 0 0 0 0 0 0 2]]
  (#'grid/write-complete-grid-row multi-page-grid 5 [1 2 3 4 5 6 7 8])
    => [[1 0 0 0 0 0 0 1]
        [1 2 3 4 5 6 7 8]])
