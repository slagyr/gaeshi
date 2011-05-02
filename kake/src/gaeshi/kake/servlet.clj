(ns gaeshi.kake.servlet
  (:require
    [clojure.set :as set])
  (:use
    [ring.util.servlet :as rs :only (build-request-map merge-servlet-keys)]
    [ring.middleware.params :only (wrap-params)]
    [ring.middleware.keyword-params :only (wrap-keyword-params)]
    [ring.middleware.cookies :only (wrap-cookies)]
    [gaeshi.env :only (development-env?)]
    [gaeshi.middleware.keyword-cookies :only (wrap-keyword-cookies)]
    [gaeshi.middleware.multipart-params :only (wrap-multipart-params)]
    [gaeshi.middleware.servlet-session :only (wrap-servlet-session)]
    [gaeshi.middleware.flash :only (wrap-flash)]
    [gaeshi.middleware.request :only (wrap-bind-request)])
  (:import
    [javax.servlet.http HttpServlet HttpServletRequest HttpServletResponse]
    [gaeshi.kake GaeshiServlet]))

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

(defn- attempt-to-load-var [ns-sym var-sym]
  (try
    (require ns-sym)
    (let [ns (the-ns ns-sym)]
      (ns-resolve ns var-sym))
    (catch Exception e
      (println "Failed to load var:" var-sym "from ns:" ns-sym e)
      nil)))

(defn- attempt-wrap [handler ns-sym var-sym]
  (if-let [wrapper (attempt-to-load-var ns-sym var-sym)]
    (wrapper handler)
    (do
      (println "Bypassing" var-sym)
      handler)))

(defn build-gaeshi-handler [handler]
  (let [handler (if (development-env?) (attempt-wrap handler 'gaeshi.middleware.verbose 'wrap-verbose) handler)]
    (->
      handler
      wrap-bind-request
      wrap-keyword-params
      wrap-params
      wrap-multipart-params
      wrap-flash
      wrap-keyword-cookies
      wrap-cookies
      wrap-servlet-session)))

(defn extract-gaeshi-handler []
  (let [core-namespace (System/getProperty "gaeshi.core.namespace")
        core-ns-sym (symbol core-namespace)
        _ (require core-ns-sym)
        core-ns (the-ns core-ns-sym)]
    (if-let [gaeshi-handler (ns-resolve core-ns 'gaeshi-handler)]
      gaeshi-handler
      (if-let [app-handler (ns-resolve core-ns 'app-handler)]
        (build-gaeshi-handler app-handler)
        (throw (Exception. (str core-namespace " must define app-handler or gaeshi-handler")))))))

(defprotocol HandlerInstallable
  (install-handler [_ handler]))

(extend-type GaeshiServlet
  HandlerInstallable
  (install-handler [this handler]
    (.setServiceMethod this (make-service-method handler))))

(defn initialize-gaeshi-servlet [servlet]
  (let [handler (extract-gaeshi-handler)
        handler (if (development-env?) (attempt-wrap handler 'gaeshi.middleware.refresh 'wrap-refresh) handler)]
    (install-handler servlet handler)))

