(ns gaeshi.support.servlet
  (:require
    [ring.util.servlet :as rs]
    [clojure.set :as set])
  (:use
    [fresh.core :only (freshener ns-to-file)]))

(defn make-service-method [handler]
  (rs/make-service-method handler))

(defn- files-to-keep-fresh []
  (filter identity (map #(ns-to-file (.name %)) (all-ns))))

(defn- report-refresh [report]
  (when-let [reloaded (seq (:reloaded report))]
    (println "Reloading...")
    (doseq [file reloaded] (println file))
    (println ""))
  true)

(def refresh! (freshener files-to-keep-fresh report-refresh))

