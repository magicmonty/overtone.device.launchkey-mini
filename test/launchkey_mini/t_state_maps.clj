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
    (#'sm/empty-state-map) => {:modes { :default {}
                                        :sequencer {}
                                        :sampler {}}}))

(fact "active-mode? returns true for the currently active mode"
  (#'sm/active-mode? test-state :default) => true

  (#'sm/active-mode? (atom {:active :sequencer }) :sequencer) => true)

(fact "active-mode? returns false for inactive mode"
  (#'sm/active-mode? (atom {
    :active :default
    :modes { :default {} :sequencer {}}}) :sequencer) => false)

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
