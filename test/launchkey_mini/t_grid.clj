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
