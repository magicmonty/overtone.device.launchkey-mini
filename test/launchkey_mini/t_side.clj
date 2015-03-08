(ns launchkey-mini.t-side
  (:require [midje.sweet :refer :all]
            [launchkey-mini.side :as side]))

(fact "empty-side produces one virtual set with not set cells"
  (#'side/empty-side) => [[0 0]])

(fact "cell gets the correct cell content"
  (#'side/cell [[1 2] [3 4]] 0 0) => 1
  (#'side/cell [[1 2] [3 4]] 1 0) => 2
  (#'side/cell [[1 2] [3 4]] 0 1) => 3
  (#'side/cell [[1 2] [3 4]] 1 1) => 4)
