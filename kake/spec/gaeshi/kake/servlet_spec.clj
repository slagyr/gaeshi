(ns gaeshi.kake.servlet-spec
  (:use
    [speclj.core]
    [gaeshi.kake.servlet :only (install-handler initialize-gaeshi-servlet)]
    [gaeshi.middleware.development :only (wrap-development)]
    [gaeshi.env :only (env)]))
(deftype FakeServlet []
  gaeshi.kake.servlet.HandlerInstallable
  (install-handler [this handler] handler))

(describe "Servlet methods"

  (before (System/setProperty "gaeshi.core.namespace" "gaeshi.kake.test-core"))
  (around [it]
    (binding [wrap-development (fn [handler] (fn [request] (assoc (handler request) :development true)))]
      (it)))

  (it "initializes a servlet in non-development mode"
    (alter-var-root #'env (fn [_] "blah"))
    (let [handler (initialize-gaeshi-servlet (FakeServlet.))
          response (handler {})]
      (should= true (:processed response))
      (should= nil (:development response))))

  (it "initializes a servlet in development mode"
    (alter-var-root #'env (fn [_] "development"))
    (let [handler (initialize-gaeshi-servlet (FakeServlet.))
          response (handler {})]
      (should= true (:processed response))
      (should= true (:development response))))

  )

(run-specs)
