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