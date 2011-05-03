(ns leiningen.generate-spec
  (:use
    [speclj.core]
    [leiningen.generate :only (generate create-templater)])
  (:import
    [java.io File]
    [filecabinet FakeFileSystem Templater Templater$TemplaterLogger]))

(describe "Generate"

  (with fs (FakeFileSystem/installed))

  (it "creates a templater"
    (let [templater (create-templater {:root "/foo/bar"})]
      (should= "/foo/bar" (.getDestinationRoot templater))
      (should= (.getCanonicalPath (File. "resources/gaeshi/tsukuri/templates"))
        (.getSourceRoot templater))))

  (context "default"

    (with logger
      (proxy [Templater$TemplaterLogger] []
        (say [message])))
    (around [it]
      (binding [create-templater
                (fn [project]
                  (let [templater (Templater. (:root project) "/templates")]
                    (.setLogger templater @logger)
                    templater))]
        (it)))
    (before
      (.createDirectory @fs "/default/root")
      (.createTextFile @fs "/templates/config/env/appengine-web.xml" "appengine: !-APP_NAME-!!-ENV_SUFFIX-!:!-ENVIRONMENT-!")
      (.createTextFile @fs "/templates/config/env/logging.properties" "lumberjacks ho!")
      (.createTextFile @fs "/templates/config/env/repl_init.clj" "repl_init: !-APP_NAME-!-!-ENVIRONMENT-!")
      (.createTextFile @fs "/templates/config/env/web.xml" "web.xml: !-APP_NAME-!")
      (.createTextFile @fs "/templates/public/javascript/default.js" "javascript")
      (.createTextFile @fs "/templates/public/stylesheets/default.css" "css")
      (.createTextFile @fs "/templates/spec/app/core_spec.clj" "core-spec: !-APP_NAME-!")
      (.createTextFile @fs "/templates/src/app/core.clj" "core: !-APP_NAME-!")
      (.createTextFile @fs "/templates/src/app/view/view_helpers.clj" "view-helpers")
      (.createTextFile @fs "/templates/src/app/view/layout.hiccup.clj" "layout")
      (.createTextFile @fs "/templates/src/app/view/index.hiccup.clj" "index")
      (.createTextFile @fs "/templates/src/app/view/not_found.hiccup.clj" "not_found")
      )
    (before (generate {:root "/default/root" :name "App"}))

    (it "generates development configuration"
      (should= "appengine: app-development:development" (.readTextFile @fs "/default/root/config/development/appengine-web.xml"))
      (should= "web.xml: app" (.readTextFile @fs "/default/root/config/development/web.xml"))
      (should= "lumberjacks ho!" (.readTextFile @fs "/default/root/config/development/logging.properties"))
      (should= "repl_init: app-development" (.readTextFile @fs "/default/root/config/development/repl_init.clj")))

    (it "generates production configuration"
      (should= "appengine: app:production" (.readTextFile @fs "/default/root/config/production/appengine-web.xml"))
      (should= "web.xml: app" (.readTextFile @fs "/default/root/config/production/web.xml"))
      (should= "lumberjacks ho!" (.readTextFile @fs "/default/root/config/production/logging.properties")))

    (it "generated public dirs"
      (should= true (.exists @fs "/default/root/public/images"))
      (should= "javascript" (.readTextFile @fs "/default/root/public/javascript/app.js"))
      (should= "css" (.readTextFile @fs "/default/root/public/stylesheets/app.css")))

    (it "generates default spec"
      (should= "core-spec: app" (.readTextFile @fs "/default/root/spec/app/core_spec.clj")))

    (it "generates default src"
      (should= "core: app" (.readTextFile @fs "default/root/src/app/core.clj"))
      (should= "view-helpers" (.readTextFile @fs "default/root/src/app/view/view_helpers.clj"))
      (should= "layout" (.readTextFile @fs "default/root/src/app/view/layout.hiccup.clj"))
      (should= "index" (.readTextFile @fs "default/root/src/app/view/index.hiccup.clj"))
      (should= "not_found" (.readTextFile @fs "default/root/src/app/view/not_found.hiccup.clj"))
      (should= true (.exists @fs "/default/root/src/app/controller"))
      (should= true (.exists @fs "/default/root/src/app/model")))
    )
  )
