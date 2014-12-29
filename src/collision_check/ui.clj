(ns collision-check.ui
  (:require [seesaw.core :as s]
            [collision-check.operations :as ops]))

(def root (atom nil))

(defn parse-fn
  "Generic function to parse text-fields into doubles. Uses a default value if
   parsing fails."
  [text-field default-value show-alert?]
  (let [val (s/text text-field)
        id (s/id-of text-field)]
    (try (Double/parseDouble val)
      (catch NumberFormatException _
        (when show-alert?
          (s/invoke-now
            (s/alert @root (str "Invalid value entered in : " id
                                "\nReplaced with " default-value
                                " for calculation."))))
        default-value))))

(defn parse-asset-pos
  "Parse user input from UI for asset position. Defaults to zero."
  [show-alert?]
  (map #(parse-fn % 0 show-alert?) [(s/select @root [:#asset-pos-u])
                                    (s/select @root [:#asset-pos-v])
                                    (s/select @root [:#asset-pos-w])]))


(defn parse-sat-pos
  "Parse user input from UI for satellite position. Defaults to zero."
  [show-alert?]
  (map #(parse-fn % 0 show-alert?) [(s/select @root [:#satellite-pos-u])
                                    (s/select @root [:#satellite-pos-v])
                                    (s/select @root [:#satellite-pos-w])]))

(defn parse-asset-cov
  "Parse user input from UI for asset covariance. Defaults to zero."
  [show-alert?]
  (let [cov-one (map #(parse-fn % 0 show-alert?)
                     [(s/select @root [:#asset-cov-00])
                      (s/select @root [:#asset-cov-01])
                      (s/select @root [:#asset-cov-02])])
        cov-two (map #(parse-fn % 0 show-alert?)
                     [(s/select @root [:#asset-cov-10])
                      (s/select @root [:#asset-cov-11])
                      (s/select @root [:#asset-cov-12])])
        cov-three (map #(parse-fn % 0 show-alert?)
                       [(s/select @root [:#asset-cov-20])
                        (s/select @root [:#asset-cov-21])
                        (s/select @root [:#asset-cov-22])])]
    [cov-one cov-two cov-three]))

(defn parse-sat-cov
  "Parse user input from UI for satellite covariance. Defaults to zero."
  [show-alert?]
  (let [cov-one (map #(parse-fn % 0 show-alert?)
                     [(s/select @root [:#satellite-cov-00])
                      (s/select @root [:#satellite-cov-01])
                      (s/select @root [:#satellite-cov-02])])
        cov-two (map #(parse-fn % 0 show-alert?)
                     [(s/select @root [:#satellite-cov-10])
                      (s/select @root [:#satellite-cov-11])
                      (s/select @root [:#satellite-cov-12])])
        cov-three (map #(parse-fn % 0 show-alert?)
                       [(s/select @root [:#satellite-cov-20])
                        (s/select @root [:#satellite-cov-21])
                        (s/select @root [:#satellite-cov-22])])]
    [cov-one cov-two cov-three]))

(defn parse-combined-radii
  "Parse user input from UI for combined satellite radii.  Defaults to 100
   meters."
  [show-alert?]
  (parse-fn (s/select @root [:#combined-radii]) 100 show-alert?))

(defn parse-sigma
  "Parse user input from UI for dispersion (standard deviation). Defaults to 1."
  [show-alert?]
  (parse-fn (s/select @root [:#sigma]) 1 show-alert?))

(defn parse-samples
  "Parse user input from UI for collision space samples. Defaults to 250,000."
  [show-alert?]
  (int (parse-fn (s/select @root [:#samples]) 250000 show-alert?)))

(defn valid-input?
  "Return true if all user input is valid. Provides error alerts if invalid."
  []
  (let [asset-pos (parse-asset-pos false)
        asset-cov (parse-asset-cov false)
        sat-pos (parse-sat-pos false)
        sat-cov (parse-sat-cov false)
        rad (parse-combined-radii false)
        sigma (parse-sigma false)
        samples (parse-samples false)
        return-valid (atom true)
        set-false (fn [] (reset! return-valid false))]
    (when-not (ops/symmetric? asset-cov)
      (s/invoke-now (s/alert @root "Asset covariance must be symmetrical."))
      (set-false))
    (when-not (ops/symmetric? sat-cov)
      (s/invoke-now (s/alert @root "Satellite covariance must be symmetrical."))
      (set-false))
    (when-not (ops/positive-definite? asset-cov)
      (s/invoke-now (s/alert @root
                             "Asset covariance must be positive-definite."))
      (set-false))
    (when-not (ops/positive-definite? sat-cov)
      (s/invoke-now (s/alert @root
                             "Satellite covariance must be positive-definite."))
      (set-false))
    (when-not (<= (ops/euclid-dist asset-pos sat-pos) 20000)
      (s/invoke-now (s/alert @root "Miss Distance is greater than 20 km."))
      (set-false))
    (when-not (pos? rad)
      (s/invoke-now (s/alert @root
                             "The combined radii must be greater than zero."))
      (set-false))
    (when-not (>= sigma 1)
      (s/invoke-now (s/alert @root
                             "Sigma must be greater than or equal to one."))
      (set-false))
    (when-not (>= samples 1000)
      (s/invoke-now (s/alert @root
                             "Samples must be greater than or equal to 1,000."))
      (set-false))
    @return-valid))

(defn sample-fn
  "Sample collision space, and output probability of collision."
  [& _]
  (s/invoke-now (s/config! (s/select @root [:#run-button]) :enabled? false))
  (s/invoke-now (s/config! (s/select @root [:#progress])
                           :indeterminate? true))
  (when (valid-input?)
    (s/invoke-now (s/config! (s/select @root [:#progress])
                             :indeterminate? false))
    (s/invoke-now (s/config! (s/select @root [:#progress]) :value 0))
    (let [asset-pos (parse-asset-pos true)
          asset-cov (parse-asset-cov true)
          sat-pos (parse-sat-pos true)
          sat-cov (parse-sat-cov true)
          rad (parse-combined-radii true)
          sigma (parse-sigma true)
          samples (parse-samples true)
          results (loop [miss-dist [] asset-points [] sat-points []]
                    (if (= (count miss-dist) samples)
                      {:miss-distances miss-dist
                       :asset-points asset-points
                       :sat-points sat-points}
                      (do
                        (s/invoke-now
                          (s/config! (s/select @root [:#progress])
                                     :value (int (* (/ (count miss-dist)
                                                       samples) 100))))
                        (let [sample-map (ops/sample-space
                                           asset-pos asset-cov
                                           sat-pos sat-cov sigma)]
                          (recur
                            (conj miss-dist (:miss-distance sample-map))
                            (conj asset-points (:asset-point sample-map))
                            (conj sat-points (:sat-point sample-map)))))))]
      (s/invoke-now (s/config! (s/select @root [:#progress])
                               :indeterminate? true))
      (ops/display-point-result (:asset-points results)
                                (:sat-points results) 2500)
      (ops/display-cdf-result (:miss-distances results) asset-pos sat-pos rad)))
  (s/invoke-now (s/config! (s/select @root [:#progress])
                           :indeterminate? false))
  (s/invoke-now (s/config! (s/select @root [:#progress]) :value 0))
  (s/invoke-now (s/config! (s/select @root [:#run-button]) :enabled? true)))

(defn input-panel
  "Create panel for Conjunction Summary Message input."
  []
  (let [asset-pos-u (s/text :id :asset-pos-u :halign :center)
        asset-pos-v (s/text :id :asset-pos-v :halign :center)
        asset-pos-w (s/text :id :asset-pos-w :halign :center)
        satellite-pos-u (s/text :id :satellite-pos-u :halign :center)
        satellite-pos-v (s/text :id :satellite-pos-v :halign :center)
        satellite-pos-w (s/text :id :satellite-pos-w :halign :center)
        asset-cov-00 (s/text :id :asset-cov-00 :halign :center)
        asset-cov-01 (s/text :id :asset-cov-01 :halign :center)
        asset-cov-02 (s/text :id :asset-cov-02 :halign :center)
        asset-cov-10 (s/text :id :asset-cov-10 :halign :center)
        asset-cov-11 (s/text :id :asset-cov-11 :halign :center)
        asset-cov-12 (s/text :id :asset-cov-12 :halign :center)
        asset-cov-20 (s/text :id :asset-cov-20 :halign :center)
        asset-cov-21 (s/text :id :asset-cov-21 :halign :center)
        asset-cov-22 (s/text :id :asset-cov-22 :halign :center)
        satellite-cov-00 (s/text :id :satellite-cov-00 :halign :center)
        satellite-cov-01 (s/text :id :satellite-cov-01 :halign :center)
        satellite-cov-02 (s/text :id :satellite-cov-02 :halign :center)
        satellite-cov-10 (s/text :id :satellite-cov-10 :halign :center)
        satellite-cov-11 (s/text :id :satellite-cov-11 :halign :center)
        satellite-cov-12 (s/text :id :satellite-cov-12 :halign :center)
        satellite-cov-20 (s/text :id :satellite-cov-20 :halign :center)
        satellite-cov-21 (s/text :id :satellite-cov-21 :halign :center)
        satellite-cov-22 (s/text :id :satellite-cov-22 :halign :center)
        combined-radii (s/text :id :combined-radii :halign :center)
        sigma (s/text :id :sigma :halign :center)
        iteration-input (s/text :halign :center :id :samples)
        run-button (s/button :text "Run" :id :run-button)
        progress-indicator (s/progress-bar :id :progress)]
    (s/listen run-button :action (fn [_] (future (sample-fn))))
    (s/vertical-panel :items [(s/horizontal-panel
                                :items ["Asset Position (m): "])
                              (s/grid-panel :columns 3 :rows 1
                                            :items [asset-pos-u
                                                    asset-pos-v
                                                    asset-pos-w])
                              (s/horizontal-panel
                                :items ["Satellite Position (m): "])
                              (s/grid-panel :columns 3 :rows 1
                                            :items [satellite-pos-u
                                                    satellite-pos-v
                                                    satellite-pos-w])
                              (s/horizontal-panel
                                :items ["Asset Covariance: "])
                              (s/grid-panel :rows 3 :columns 3
                                            :items [asset-cov-00
                                                    asset-cov-01
                                                    asset-cov-02
                                                    asset-cov-10
                                                    asset-cov-11
                                                    asset-cov-12
                                                    asset-cov-20
                                                    asset-cov-21
                                                    asset-cov-22])
                              (s/horizontal-panel
                                :items ["Satellite Covariance: "])
                              (s/grid-panel :rows 3 :columns 3
                                            :items [satellite-cov-00
                                                    satellite-cov-01
                                                    satellite-cov-02
                                                    satellite-cov-10
                                                    satellite-cov-11
                                                    satellite-cov-12
                                                    satellite-cov-20
                                                    satellite-cov-21
                                                    satellite-cov-22])
                              (s/grid-panel :columns 2 :rows 1
                                            :items ["Combined Radii (m): "
                                                    combined-radii])
                              (s/grid-panel :columns 2 :rows 1
                                            :items ["Sigma: "
                                                    sigma])
                              (s/grid-panel :columns 2 :rows 1
                                :items ["Samples: " iteration-input])
                              (s/horizontal-panel
                                :items [progress-indicator])
                              (s/grid-panel :items [run-button])])))

(defn ui-panel
  "Create main interface panel."
  []
  (reset! root (input-panel)))
