(ns gaeshi.support.servlet
  (:require
    [clojure.set :as set])
  (:use
    [ring.util.servlet :as rs :only (build-request-map merge-servlet-keys)]
    [gaeshi.env :only (development-env?)])
  (:import
    [javax.servlet.http
     HttpServlet
     HttpServletRequest
     HttpServletResponse]))

(defn update-servlet-response [^HttpServletResponse response, response-map]
  (when (not (and response (or (.isCommitted response) (:ignore-response response-map))))
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

(defn- extract-gaeshi-handler []
  (let [core-namespace (System/getProperty "gaeshi.core.namespace")
        core-ns-sym (symbol core-namespace)
        _ (require core-ns-sym)
        core-ns (the-ns core-ns-sym)]
    (ns-resolve core-ns (symbol "gaeshi-handler"))))

(defn- build-development-handler [handler]
  ; MDM - The reason for obscurity here is to avoid the dependencies.
  ; If we did this the typical way, the fresh jar would have to be in the classpath and it's not needed in non-development environments.
  (try
    (require 'gaeshi.middleware.refresh)
    (require 'gaeshi.middleware.verbose)
    (let [refresh-ns (the-ns 'gaeshi.middleware.refresh)
          verbose-ns (the-ns 'gaeshi.middleware.verbose)
          wrap-refresh (ns-resolve refresh-ns 'wrap-refresh)
          wrap-verbose (ns-resolve verbose-ns 'wrap-verbose)]
      (->
        handler
        wrap-verbose
        wrap-refresh))
    (catch Exception e
      (println "Failed to create development handler.  Using normal handler." e)
      handler)))

(defn initialize-gaeshi-servlet [servlet]
  (let [handler (extract-gaeshi-handler)
        handler (if (development-env?) (build-development-handler handler) handler)
        service-method (make-service-method handler)]
    (.setServiceMethod servlet service-method)))

