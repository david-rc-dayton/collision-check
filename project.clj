(defproject collision-check "3.1.4-SNAPSHOT"
  :description "Satellite collision probability estimator."
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [com.github.insubstantial/substance "7.3"]
                 [seesaw "1.4.4"]
                 [incanter "1.5.5"]]
  :javac-options ["-target" "1.7" "-source" "1.7" "-Xlint:-options"]
  :aot :all
  :omit-source true
  :main collision-check.core)
