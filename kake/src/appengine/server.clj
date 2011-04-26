(ns #^{:author "Roman Scherer"}
  appengine.server
  (:import org.mortbay.jetty.handler.ContextHandlerCollection
           org.mortbay.jetty.Server
           javax.servlet.http.HttpServlet
           java.io.File
           [org.mortbay.jetty.servlet Context ServletHolder])
  (:use appengine.environment
        [clojure.contrib.string :only (join)]
        ;; [appengine.jetty :only (run-jetty)]
        [ring.util.servlet :only (servlet)]
        [ring.adapter.jetty :only (run-jetty)]))

(defn- appengine-web-xml [directory]
  (join File/separator [directory "WEB-INF" "appengine-web.xml"]))

(defn start-server
  "Start the server.

Example:

  (start-server \"Hello World\" :directory \"/tmp\" :join? false :port 8080)
  ; => #<Server Server@1a2cc7>
"
  [application & options]
  (let [options (apply hash-map options) directory (or (:directory options) "war")]
    (with-configuration (or (:filename options) (appengine-web-xml directory))
      (dosync
       (init-appengine directory)
       (run-jetty
        (environment-decorator application)
        options)))))

(defn stop-server
  "Stop the server.

Example:

  (def *server* (start-server \"Hello World\" :directory \"/tmp\" :join? false :port 8080))
  ; => #<Server Server@1a2cc7>

  (stop-server *server*)
  ; => #<Server Server@1a2cc7>
"
  [server]
  (do (.stop server)
      server))

;; (def *server* (start-server "Hello World" :port 8080 :join? false :filename "test/fixtures/appengine-web.xml"))
;; (stop-server *server*)
