(defproject launchkey-mini "0.1.0-SNAPSHOT"
  :description "Use Novation Launchkey Mini with Overtone"
  :url "http://github.com/magicmonty/overtone.device.launchkey-mini"

  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [overtone "0.9.1"]
                 [overtone/at-at "1.2.0"]
                 [slingshot "0.10.3"]]

  :profiles {
    :dev {:dependencies [[midje "1.5.1"]]
          :plugins      [[lein-midje "3.0.1"]
                         [lein-kibit "0.0.8"]]}})
