(ns launchkey-mini.t-device
  (:require [midje.sweet :refer :all]
            [launchkey-mini.device :as device]))

(fact "coordinate->note returns correct note"
  (#'device/coordinate->note 0 0) => 0x60
  (#'device/coordinate->note 0 7) => 0x67
  (#'device/coordinate->note 1 0) => 0x70
  (#'device/coordinate->note 1 7) => 0x77)

(fact "coordinate->note wraps at x-overflow"
  (#'device/coordinate->note 0 8) => 0x60
  (#'device/coordinate->note 1 8) => 0x70)

(fact "coordinate->note wraps at y-overflow"
  (#'device/coordinate->note 2 0) => 0x60)
