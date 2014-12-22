(defproject collision-check "3.1.2"
  :description "Satellite collision probability estimator."
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [com.github.insubstantial/substance "7.3"]
                 [seesaw "1.4.4"]
                 [incanter "1.5.5"]]
  :javac-options ["-target" "1.6" "-source" "1.6" "-Xlint:-options"]
  :aot :all
  :omit-source true
  :main collision-check.core)
