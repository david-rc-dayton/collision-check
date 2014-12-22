(ns collision-check.core
  (:require [collision-check.ui :as ui]
            [seesaw.core :as s])
  (:import [org.pushingpixels.substance.api SubstanceLookAndFeel]
           [org.pushingpixels.substance.api.skin GraphiteGlassSkin]
           [javax.swing ImageIcon])
  (:gen-class))

(def title "Collision Check")

(defmacro version []
  (System/getProperty "collision-check.version"))

(def display-name (str title " " (version)))

(defn center!
  [frame]
  (.setLocationRelativeTo frame nil))

(defn -main
  [& args]
  (SubstanceLookAndFeel/setSkin (GraphiteGlassSkin.))
  (s/invoke-later
    (doto (s/frame :title display-name
                   :content (ui/ui-panel)
                   :size [450 :by 450]
                   :minimum-size [450 :by 450]
                   :icon "icon.png"
                   :on-close :exit)
      center! s/show!)))
