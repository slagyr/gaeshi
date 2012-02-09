(ns gaeshi.kake.servlet-spec
  (:use
    [speclj.core]
    [gaeshi.kake.servlet]
    [gaeshi.middleware.verbose :only (wrap-verbose)]
    [gaeshi.middleware.refresh :only (wrap-refresh)]
    [gaeshi.env :only (env)]
    [gaeshi.middleware.servlet-session :only (wrap-servlet-session)]))
(deftype FakeServlet []
  gaeshi.kake.servlet.HandlerInstallable
  (install-handler [this handler] handler))

(defn fake-wrapper [key]
  (fn [handler]
    (fn [request]
      (assoc (handler request) key true))))

(describe "Servlet methods"

  (before (System/setProperty "gaeshi.core.namespace" "gaeshi.kake.test-core"))
  (around [it]
    (binding [wrap-verbose (fake-wrapper :verbose)
              wrap-refresh (fake-wrapper :refresh)
              wrap-servlet-session (fake-wrapper :servlet-session)]
      (it)))

  (it "extracts the app handler"
    (binding [build-gaeshi-handler (fake-wrapper :fake-gaeshi-handler)]
      (let [handler (extract-gaeshi-handler)
            response (handler {})]
        (should= true (:app-handler response))
        (should= true (:fake-gaeshi-handler response)))))

  (it "extracts gaeshi-handler if it is defined"
    (System/setProperty "gaeshi.core.namespace" "gaeshi.kake.test-override-core")
    (let [handler (extract-gaeshi-handler)
          response (handler {})]
      (should-not (:app-handler response))
      (should= true (:gaeshi-handler response))))

  (it "initializes a servlet in non-development mode"
    (alter-var-root #'env (fn [_] "blah"))
    (let [handler (initialize-gaeshi-servlet (FakeServlet.))
          response (handler {})]
      (should= true (:app-handler response))
      (should= nil (:development response))))

  (it "initializes a servlet in development mode"
    (alter-var-root #'env (fn [_] "development"))
    (let [handler (initialize-gaeshi-servlet (FakeServlet.))
          response (handler {})]
      (should= true (:app-handler response))
      (should= true (:verbose response))
      (should= true (:refresh response))))

  )

(run-specs)
