(ns launchkey-mini.led-test
  (:require [midje.sweet :refer :all]
            [launchkey-mini.led :as led]))

(fact "velocity maps color and intensity to a decimal signal"
  (#'led/velocity {:color :red    :intensity 3}) => 15
  (#'led/velocity {:color :orange :intensity 3}) => 47
  (#'led/velocity {:color :yellow :intensity 3}) => 62
  (#'led/velocity {:color :green  :intensity 3}) => 60
  (#'led/velocity {:color :amber  :intensity 3}) => 63)

(fact "invalid colors return 0"
  (#'led/velocity {:color :brown :intensity 0}) => 0)

(fact "intensity is capped at 3"
  (#'led/velocity {:color :red :intensity 100000}) => 15)
