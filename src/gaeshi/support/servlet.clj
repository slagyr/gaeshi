(ns gaeshi.support.servlet
  (:require
    [clojure.set :as set])
  (:use
    [ring.util.servlet :as rs :only (build-request-map merge-servlet-keys)]
    [fresh.core :only (freshener ns-to-file)])
  (:import
    [javax.servlet.http
     HttpServlet
     HttpServletRequest
     HttpServletResponse]))

(defn update-servlet-response [^HttpServletResponse response, response-map]
  (when (not (and response (.isCommitted response)))
    (rs/update-servlet-response response response-map)))

(defn make-service-method
  "Turns a handler into a function that takes the same arguments and has the
  same return value as the service method in the HttpServlet class."
  [handler]
  (fn [^HttpServlet servlet
       ^HttpServletRequest request
       ^HttpServletResponse response]
    (.setCharacterEncoding response "UTF-8")
    (let [request-map (-> request
      (build-request-map)
      (merge-servlet-keys servlet request response))]
      (if-let [response-map (handler request-map)]
        (update-servlet-response response response-map)
        (throw (NullPointerException. "Handler returned nil"))))))


(defn- files-to-keep-fresh []
  (filter identity (map #(ns-to-file (.name %)) (all-ns))))

(defn- report-refresh [report]
  (when-let [reloaded (seq (:reloaded report))]
    (println "Reloading...")
    (doseq [file reloaded] (println file))
    (println ""))
  true)

(def refresh! (freshener files-to-keep-fresh report-refresh))

