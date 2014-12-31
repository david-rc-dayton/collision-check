(ns collision-check.operations
  (:require [incanter.core :as i]
            [incanter.charts :as ic]
            [incanter.stats :as is]
            [seesaw.core :as s]))

(def rgen (java.util.Random.))

(defn gauss-matrix
  "Generate a normally distributed 3x1 column vector."
  []
  (i/matrix [(.nextGaussian rgen) (.nextGaussian rgen) (.nextGaussian rgen)]))

(defn symmetric?
  "Return true if a 3x3 list of lists is symmetric along the identity."
  [matrix]
  (let [tri-fst (= (get-in matrix [1 0]) (get-in matrix [0 1]))
        tri-snd (= (get-in matrix [2 0]) (get-in matrix [0 2]))
        tri-thd (= (get-in matrix [1 2]) (get-in matrix [2 1]))]
    (and tri-fst tri-snd tri-thd)))

(defn positive-definite?
  "Return true if a 3x3 list of lists is positive-definite."
  [matrix]
  (let [eig (i/decomp-eigenvalue (i/matrix matrix))]
    (every? pos? (:values eig))))

(defn euclid-dist
  "Return the euclidean between two lists containing three elements."
  [list-one list-two]
  (let [delta-u (Math/pow (- (nth list-one 0) (nth list-two 0)) 2)
        delta-v (Math/pow (- (nth list-one 1) (nth list-two 1)) 2)
        delta-w (Math/pow (- (nth list-one 2) (nth list-two 2)) 2)]
    (Math/sqrt (+ delta-u delta-v delta-w))))

(defn scott-bins
  "Determine the optimal number of histogram bins based on Scott's Rule."
  [values]
  (let [sigma (is/sd values)
        cube-root (Math/cbrt (count values))
        high (apply max values)]
    (int (/ high (/ (* 3.5 sigma) cube-root)))))

(defn sample-space
  "Sample collision space, using the asset/satellite position as a list,
  covariance as a list of lists, and the standard deviation. Returns the miss
  distance between the asset and conjuncting satellite."
  [asset-pos asset-cov sat-pos sat-cov std-dev]
  (let [asset-vec (i/matrix asset-pos)
        asset-gauss (gauss-matrix)
        asset-chol (i/decomp-cholesky (i/mult (i/matrix asset-cov) std-dev))
        sat-vec (i/matrix sat-pos)
        sat-gauss (gauss-matrix)
        sat-chol (i/decomp-cholesky (i/mult (i/matrix sat-cov) std-dev))
        asset-prime (i/plus (i/mmult asset-chol asset-gauss) asset-vec)
        sat-prime (i/plus (i/mmult sat-chol sat-gauss) sat-vec)]
    {:miss-distance (euclid-dist (i/to-vect asset-prime) (i/to-vect sat-prime))
     :asset-point (i/to-vect asset-prime) :sat-point (i/to-vect sat-prime)}))

(defn cdf-dist
  "Produce a map of containing the values and their associated cumulative
   distributions. This function also takes the number of boxes as an argument."
  [values nboxes]
  (let [high (reduce max values)
        step (/ high nboxes)
        rng (range 0 (+ step high) step)
        cdf-fn (fn [n] (double (/ (count (filter #(<= % n) values))
                                  (count values))))]
    {:cdf-x (map double rng) :cdf-y (map cdf-fn rng)}))

(defn display-cdf-result
  "Create and display a chart containing the probability of collision,
   cumulative distribution of miss distances, Conjuntion Summary Message miss
   distance, and an indicator for the combined radii."
  [miss-distances asset-pos sat-pos collision-radii]
  (let [coll-prob (/ (count (filter #(<= % collision-radii) miss-distances))
                     (count miss-distances))
        max-miss (apply max miss-distances)
        cdf-map (cdf-dist miss-distances (scott-bins miss-distances))
        chart (ic/xy-plot (:cdf-x cdf-map) (:cdf-y cdf-map))]
    (ic/set-x-range chart 0 max-miss)
    (ic/set-y-range chart 0 1)
    (ic/set-x-label chart "Miss Distance (m)")
    (ic/set-y-label chart "Cumulative Probability")
    (ic/add-lines chart [collision-radii collision-radii] [0 1])
    (ic/add-text chart (* max-miss 0.75) 0.08
                 (format "CSM Miss Distance (m): %d"
                         (int (euclid-dist asset-pos sat-pos))))
    (ic/add-text chart (* max-miss 0.75) 0.05
                 (format "Collision Probability: %.0e" (double coll-prob)))
    (s/invoke-now
     (i/view chart :width 450 :height 450
             :window-title "Miss-Distance Cumulative Distribution"))))

(defn display-point-result
  "Create and display a chart containing a scatter point representation of the
   sampled points, showing the conjunction space."
  [asset-points sat-points plotted-points]
  (let [alpha 0.2
        asset-u (take plotted-points (map #(nth % 0) asset-points))
        asset-v (take plotted-points (map #(nth % 1) asset-points))
        asset-w (take plotted-points (map #(nth % 2) asset-points))
        sat-u (take plotted-points (map #(nth % 0) sat-points))
        sat-v (take plotted-points (map #(nth % 1) sat-points))
        sat-w (take plotted-points (map #(nth % 2) sat-points))
        chart-vw (-> (ic/scatter-plot asset-v asset-w)
                     (ic/add-points sat-v sat-w) (ic/set-alpha alpha)
                     (ic/set-x-label "In-Track Axis (m)")
                     (ic/set-y-label "Cross-Track Axis (m)"))
        chart-uw (-> (ic/scatter-plot asset-u asset-w)
                     (ic/add-points sat-u sat-w) (ic/set-alpha alpha)
                     (ic/set-x-label "Radial Axis (m)")
                     (ic/set-y-label "Cross-Track Axis (m)"))
        chart-uv (-> (ic/scatter-plot asset-u asset-v)
                     (ic/add-points sat-u sat-v) (ic/set-alpha alpha)
                     (ic/set-x-label "Radial Axis (m)")
                     (ic/set-y-label "In-Track Axis (m)"))]
    (s/invoke-now (i/view chart-vw :width 450 :height 450
                          :window-title "VW-Axis Conjunction Space"))
    (s/invoke-now (i/view chart-uw :width 450 :height 450
                          :window-title "UW-Axis Conjunction Space"))
    (s/invoke-now (i/view chart-uv :width 450 :height 450
                          :window-title "UV-Axis Conjunction Space"))))
