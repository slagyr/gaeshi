(ns gaeshi.middleware.refresh
  (:use
    [fresh.core :only (freshener ns-to-file)]))

(defn- files-to-keep-fresh []
  (filter identity (map #(ns-to-file (.name %)) (all-ns))))

(defn- report-refresh [report]
  (when-let [reloaded (seq (:reloaded report))]
    (println "Reloading...")
    (doseq [file reloaded] (println file))
    (println ""))
  true)

(defn wrap-refresh [handler]
  (let [refresh! (freshener files-to-keep-fresh report-refresh)]
    (fn [request]
      (refresh!)
      (handler request))))
