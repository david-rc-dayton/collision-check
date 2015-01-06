(defproject collision-check "3.1.4"
  :description "Satellite collision probability estimator."
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [com.github.insubstantial/substance "7.3"]
                 [seesaw "1.4.5"]
                 [incanter "1.5.6"]]
  :profiles {:uberjar {:aot :all
                       :omit-source true}}
  :javac-options ["-target" "1.7" "-source" "1.7" "-Xlint:-options"]
  :aot [collision-check.core]
  :main collision-check.core)
