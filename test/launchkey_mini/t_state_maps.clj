(ns launchkey-mini.t-state-maps
  (:require [midje.sweet :refer :all]
            [launchkey-mini.state-maps :as sm]
            [launchkey-mini.grid :as g]
            [launchkey-mini.side :as s]))

(def test-state (atom (#'sm/empty-state-map)))

(fact "mode returns the currently active mode"
      (#'sm/mode test-state) => :default)

(fact "mode? returns true if mode exists"
      (#'sm/mode? test-state :default) => true)

(fact "mode? returns false if mode does not exist"
      (#'sm/mode? test-state :foo) => false)

(fact "modes returns a list of available modes"
      (#'sm/modes test-state) => [:default]

      (#'sm/modes (atom (#'sm/empty-state-map))) => [:default :sequencer :sampler]
      (provided
        (#'sm/empty-state-map) => {:modes
                                   {:default {}
                                    :sequencer {}
                                    :sampler {}}}))

(fact "active-mode? returns true for the currently active mode"
      (#'sm/active-mode? test-state :default) => true
      (#'sm/active-mode? (atom {:active :sequencer }) :sequencer) => true)

(fact "active-mode? returns false for inactive mode"
      (#'sm/active-mode? (atom {
                                :active :default
                                :modes {:default {}
                                        :sequencer {}}})
                         :sequencer) => false)

(fact "active-mode? returns false for unknown mode"
      (#'sm/active-mode? test-state :foo) => false)

(fact "add-mode adds new mode"
      (#'sm/add-mode (atom {:modes { :default ..mode.. }}) :foo) => {:modes { :default ..mode.. :foo ..mode.. }}
      (provided
        (#'sm/empty-mode) => ..mode..))

(fact "add-mode does not replace existing mode"
      (#'sm/add-mode (atom {:modes { :default ..mode.. }}) :default) => {:modes { :default ..mode.. }}
      (provided
        (#'sm/empty-mode) => irrelevant :times 0))

(fact "active-side returns side from active mode"
      (#'sm/active-side (atom {:active :default
                               :modes {:default {:side ..defaultside..}}})) => ..defaultside..)

(def multigrid [[0 0 0 0 0 0 0 0 1 1 1 1 1 1 1 1]
                [0 0 0 0 0 0 0 0 1 1 1 1 1 1 1 1]
                [2 2 2 2 2 2 2 2 3 3 3 3 3 3 3 3]
                [2 2 2 2 2 2 2 2 3 3 3 3 3 3 3 3]])

(fact "active-grid returns grid from active mode"
      (#'sm/active-grid (atom {:active :default
                               :modes {:default {:grid multigrid}}})) => multigrid)


(fact "active-page returns active-page"
      (#'sm/active-page (atom {:active :default
                               :modes {:default {:grid multigrid}}
                               :page-coords [0 0]})) => [[0 0 0 0 0 0 0 0]
                                                         [0 0 0 0 0 0 0 0]]
      (#'sm/active-page (atom {:active :default
                               :modes {:default {:grid multigrid}}
                               :page-coords [1 0]})) => [[1 1 1 1 1 1 1 1]
                                                         [1 1 1 1 1 1 1 1]]
      (#'sm/active-page (atom {:active :default
                               :modes {:default {:grid multigrid}}
                               :page-coords [0 1]})) => [[2 2 2 2 2 2 2 2]
                                                         [2 2 2 2 2 2 2 2]]
      (#'sm/active-page (atom {:active :default
                               :modes {:default {:grid multigrid}}
                               :page-coords [1 1]})) => [[3 3 3 3 3 3 3 3]
                                                         [3 3 3 3 3 3 3 3]])

(def multigrid (g/virtual-grid 2 2))
(fact "toggle! toggles correct led"
      @(#'sm/toggle! (atom {:active :default
                           :modes {:default {:grid multigrid}}
                           :page-coords [0 0]})
                    0 0) => {:active :default
                             :modes {:default {:grid [[1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0]
                                                      [0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0]
                                                      [0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0]
                                                      [0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0]]}}
                             :page-coords [0 0]}

      @(#'sm/toggle! (atom {:active :default
                           :modes {:default {:grid multigrid}}
                           :page-coords [1 1]})
                    7 1) => {:active :default
                             :modes {:default {:grid [[0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0]
                                                      [0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0]
                                                      [0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0]
                                                      [0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1]]}}
                             :page-coords [1 1]})

(fact "toggle-side! toggles correct led"
      @(#'sm/toggle-side! (atom {:active :default
                                 :modes {:default {:side [[0 0] [0 0]]}}
                                 :page-coords [0 0]})
                          0) => {:active :default
                                 :modes {:default {:side [[1 0] [0 0]]}}
                                 :page-coords [0 0]}

      @(#'sm/toggle-side! (atom {:active :default
                                 :modes {:default {:side [[0 0] [0 0]]}}
                                 :page-coords [1 1]})
                          1) => {:active :default
                                 :modes {:default {:side [[0 0] [0 1]]}}
                                 :page-coords [1 1]})

(fact "trigger-fn returns correct function"
      (#'sm/trigger-fn (atom {:active :default
                              :fn-map {:default-0x1 {:3x1 ..handler..}}
                              :page-coords [0 1]})
                        3 1) => ..handler..

      (#'sm/trigger-fn (atom {:active :default
                              :fn-map {:default-1x0 {:row1 ..handler..}}
                              :page-coords [1 0]})
                        :row1) => ..handler..)

(def multigrid [[0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1]
                [0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0]
                [1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0]
                [0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0]])

(fact "on? returns correct value for given state"
      (#'sm/on? (atom {:active :default
                       :modes {:default {:grid multigrid}}
                       :page-coords [0 0]})
                7 1) => truthy
      (#'sm/on? (atom {:active :default
                       :modes {:default {:grid multigrid}}
                       :page-coords [1 0]})
                7 0) => truthy
      (#'sm/on? (atom {:active :default
                       :modes {:default {:grid multigrid}}
                       :page-coords [0 1]})
                0 0) => truthy
      (#'sm/on? (atom {:active :default
                       :modes {:default {:grid multigrid}}
                       :page-coords [1 1]})
                0 1) => truthy)

(fact "visible? returns correct value for given absolute cell coordinates in state"
      (#'sm/visible? (atom {:active :default
                            :modes {:default {:grid multigrid}}
                            :page-coords [0 0]})
                     0 0) => truthy

      (#'sm/visible? (atom {:active :default
                            :modes {:default {:grid multigrid}}
                            :page-coords [0 0]})
                     7 0) => truthy

      (#'sm/visible? (atom {:active :default
                            :modes {:default {:grid multigrid}}
                            :page-coords [0 0]})
                     8 0) => falsey

      (#'sm/visible? (atom {:active :default
                            :modes {:default {:grid multigrid}}
                            :page-coords [1 0]})
                     8 0) => truthy)

(fact "set-cell! sets the given value in the grid"
      @(#'sm/set-cell! (atom {:active :default
                              :modes {:default {:grid multigrid}}
                              :page-coords [0 0]})
                        3 1 5) => {:active :default
                                   :modes {:default {:grid [[0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1]
                                                            [0 0 0 5 0 0 0 1 0 0 0 0 0 0 0 0]
                                                            [1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0]
                                                            [0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0]]}}
                              :page-coords [0 0]}
      @(#'sm/set-cell! (atom {:active :default
                              :modes {:default {:grid multigrid}}
                              :page-coords [1 0]})
                        4 1 8) => {:active :default
                                   :modes {:default {:grid [[0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1]
                                                            [0 0 0 0 0 0 0 1 0 0 0 0 8 0 0 0]
                                                            [1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0]
                                                            [0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0]]}}
                              :page-coords [1 0]}
      @(#'sm/set-cell! (atom {:active :default
                              :modes {:default {:grid multigrid}}
                              :page-coords [0 1]})
                        7 0 2) => {:active :default
                                   :modes {:default {:grid [[0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1]
                                                            [0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0]
                                                            [1 0 0 0 0 0 0 2 0 0 0 0 0 0 0 0]
                                                            [0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0]]}}
                              :page-coords [0 1]}
      @(#'sm/set-cell! (atom {:active :default
                              :modes {:default {:grid multigrid}}
                              :page-coords [1 1]})
                        3 1 5) => {:active :default
                                   :modes {:default {:grid [[0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1]
                                                            [0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0]
                                                            [1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0]
                                                            [0 0 0 0 0 0 0 0 1 0 0 5 0 0 0 0]]}}
                              :page-coords [1 1]})


(fact "cell gets cell from current state"
      (#'sm/cell (atom {:active :default
                        :modes {:default {:grid multigrid}}
                        :page-coords [0 0]})
                 0 0) => 0
      (#'sm/cell (atom {:active :default
                        :modes {:default {:grid multigrid}}
                        :page-coords [0 0]})
                 7 0) => 0
      (#'sm/cell (atom {:active :default
                        :modes {:default {:grid multigrid}}
                        :page-coords [0 0]})
                 0 1) => 0
      (#'sm/cell (atom {:active :default
                        :modes {:default {:grid multigrid}}
                        :page-coords [0 0]})
                 7 1) => 1
      (#'sm/cell (atom {:active :default
                        :modes {:default {:grid multigrid}}
                        :page-coords [0 1]})
                 0 0) => 1)

(fact "absolute-cell gets cell value via its absolute position"
      (#'sm/absolute-cell (atom {:active :default
                                 :modes {:default {:grid multigrid}}})
                          0 0) => 0
      (#'sm/absolute-cell (atom {:active :default
                                 :modes {:default {:grid multigrid}}})
                          15 0) => 1
      (#'sm/absolute-cell (atom {:active :default
                                 :modes {:default {:grid multigrid}}})
                          7 1) => 1
      (#'sm/absolute-cell (atom {:active :default
                                 :modes {:default {:grid multigrid}}})
                          0 2) => 1
      (#'sm/absolute-cell (atom {:active :default
                                 :modes {:default {:grid multigrid}}})
                          8 3) => 1)

(fact "side-cell gets correct value"
      (#'sm/side-cell (atom {:active :default
                                 :modes {:default {:side [[0 1][2 3]]}}
                                 :page-coords [0 0]})
                       0) => 0
      (#'sm/side-cell (atom {:active :default
                                 :modes {:default {:side [[0 1][2 3]]}}
                                 :page-coords [0 0]})
                       1) => 1
      (#'sm/side-cell (atom {:active :default
                                 :modes {:default {:side [[0 1][2 3]]}}
                                 :page-coords [0 1]})
                       0) => 2
      (#'sm/side-cell (atom {:active :default
                                 :modes {:default {:side [[0 1][2 3]]}}
                                 :page-coords [0 1]})
                       1) => 3)

(fact "get-row returns correct row data"
      (#'sm/get-row (atom {:active :default
                           :modes {:default {:grid multigrid}}
                           :page-coords [0 0]})
                     0) => [0 0 0 0 0 0 0 0]
      (#'sm/get-row (atom {:active :default
                           :modes {:default {:grid multigrid}}
                           :page-coords [0 0]})
                     1) => [0 0 0 0 0 0 0 1]

      (#'sm/get-row (atom {:active :default
                           :modes {:default {:grid multigrid}}
                           :page-coords [1 0]})
                     0) => [0 0 0 0 0 0 0 1]
      (#'sm/get-row (atom {:active :default
                           :modes {:default {:grid multigrid}}
                           :page-coords [1 0]})
                     1) => [0 0 0 0 0 0 0 0]

      (#'sm/get-row (atom {:active :default
                           :modes {:default {:grid multigrid}}
                           :page-coords [0 1]})
                     0) => [1 0 0 0 0 0 0 0]
      (#'sm/get-row (atom {:active :default
                           :modes {:default {:grid multigrid}}
                           :page-coords [0 1]})
                     1) => [0 0 0 0 0 0 0 0]

      (#'sm/get-row (atom {:active :default
                           :modes {:default {:grid multigrid}}
                           :page-coords [1 1]})
                     0) => [0 0 0 0 0 0 0 0]
      (#'sm/get-row (atom {:active :default
                           :modes {:default {:grid multigrid}}
                           :page-coords [1 1]})
                     1) => [1 0 0 0 0 0 0 0])

(fact "get-column returns correct column-data"
      (#'sm/get-column (atom {:active :default
                             :modes {:default {:grid multigrid}}
                             :page-coords [0 0]})
                     7) => [0 1]
      (#'sm/get-column (atom {:active :default
                             :modes {:default {:grid multigrid}}
                             :page-coords [1 0]})
                     7) => [1 0]
      (#'sm/get-column (atom {:active :default
                             :modes {:default {:grid multigrid}}
                             :page-coords [0 1]})
                     0) => [1 0]
      (#'sm/get-column (atom {:active :default
                             :modes {:default {:grid multigrid}}
                             :page-coords [1 1]})
                     0) => [0 1])

(fact "absolute-column returns complete column"
      (#'sm/absolute-column (atom {:active :default
                                   :modes {:default {:grid multigrid}}
                                   :page-coords [0 0]})
                             7) => [0 1]
      (#'sm/absolute-column (atom {:active :default
                                   :modes {:default {:grid multigrid}}
                                   :page-coords [0 0]})
                            15) => [1 0]

      (#'sm/absolute-column (atom {:active :default
                                  :modes {:default {:grid multigrid}}
                                   :page-coords [0 1]})
                             0) => [1 0]
      (#'sm/absolute-column (atom {:active :default
                                   :modes {:default {:grid multigrid}}
                                   :page-coords [0 1]})
                            8) => [0 1])

(fact "column-off sets a complete column to 0"
      (@#'sm/column-off (atom {:active :default
                              :modes {:default {:grid [[0 1 0 0 0 0 0 0]
                                                       [0 1 0 0 0 0 0 0]
                                                       [0 1 0 0 0 0 0 0]
                                                       [0 1 0 0 0 0 0 0]]}}
                              :page-coords [0 0]})
                       1) => {:active :default
                              :modes {:default {:grid [[0 0 0 0 0 0 0 0]
                                                       [0 0 0 0 0 0 0 0]
                                                       [0 0 0 0 0 0 0 0]
                                                       [0 0 0 0 0 0 0 0]]}}
                              :page-coords [0 0]})

(fact "row-active? returns true, if side is active (by page)"
      (@#'sm/row-active? (atom{:active :default
                               :modes {:default {:side [[1 1][0 0]]}}
                               :page-coords [0 0]})
                         0) => truthy

      (@#'sm/row-active? (atom{:active :default
                               :modes {:default {:side [[1 1][0 0]]}}
                               :page-coords [0 0]})
                         1) => truthy)

(fact "row-active? returns true, if side is inactive (by page)"
      (@#'sm/row-active? (atom{:active :default
                               :modes {:default {:side [[1 1][0 0]]}}
                               :page-coords [0 1]})
                         0) => falsey

      (@#'sm/row-active? (atom{:active :default
                               :modes {:default {:side [[1 1][0 0]]}}
                               :page-coords [0 1]})
                         1) => falsey)

(fact "absolute-row-active? returns true, if side is active (absolute reference)"
      (@#'sm/absolute-row-active? (atom{:active :default
                                        :modes {:default {:side [[1 0][0 1]]}}})
                                  0) => truthy
      (@#'sm/absolute-row-active? (atom{:active :default
                                        :modes {:default {:side [[1 0][0 1]]}}})
                                  1) => falsey
      (@#'sm/absolute-row-active? (atom{:active :default
                                        :modes {:default {:side [[1 0][0 1]]}}})
                                  2) => falsey
      (@#'sm/absolute-row-active? (atom{:active :default
                                        :modes {:default {:side [[1 0][0 1]]}}})
                                  3) => truthy)

(def test-state {:active :other
                 :modes {:default ..changed-default-state..
                         :other   ..other-state..}
                 :page-coords [2 3]})

(fact "reset-state! resets state to empty state"
      (@#'sm/reset-state! (atom test-state)) => (#'sm/empty-state-map))

(fact "reset-page-position resets page coordinates"
      (@#'sm/reset-page-position (atom test-state)) => {:active :other
                                                        :modes {:default ..changed-default-state..
                                                                :other   ..other-state..}
                                                        :page-coords [0 0]})

(fact "set-page-x sets x-coordinate of page"
      (@#'sm/set-page-x (atom test-state) 0) => {:active :other
                                                 :modes {:default ..changed-default-state..
                                                         :other   ..other-state..}
                                                 :page-coords [0 3]})

(fact "set-page-y sets y-coordinate of page"
      (@#'sm/set-page-y (atom test-state) 0) => {:active :other
                                                 :modes {:default ..changed-default-state..
                                                         :other   ..other-state..}
                                                 :page-coords [2 0]})

(fact "set-page sets x- and y-coordinate of page"
      (@#'sm/set-page (atom test-state) 1 4) => {:active :other
                                                 :modes {:default ..changed-default-state..
                                                         :other   ..other-state..}
                                                 :page-coords [1 4]})

(fact "shift-page-left decrements x-coordinate of page"
      (@#'sm/shift-page-left (atom {:page-coords [2 3]})) => {:page-coords [1 3]})

(fact "shift-page-left does not decrement x-coordinate of page beyond 0"
      (@#'sm/shift-page-left (atom {:page-coords [1 3]})) => {:page-coords [0 3]}
      (@#'sm/shift-page-left (atom {:page-coords [0 3]})) => {:page-coords [0 3]})

(fact "shift-page-up decrements y-coordinate of page"
      (@#'sm/shift-page-up (atom {:page-coords [2 3]})) => {:page-coords [2 2]})

(fact "shift-page-up does not decrement y-coordinate of page beyond 0"
      (@#'sm/shift-page-up (atom {:page-coords [2 1]})) => {:page-coords [2 0]}
      (@#'sm/shift-page-up (atom {:page-coords [2 0]})) => {:page-coords [2 0]})
