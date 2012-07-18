(ns gaeshi.kuzushi.commands.new-spec
  (:use
    [speclj.core]
    [gaeshi.kuzushi.commands.new :only (execute parse-args)]
    [joodo.kuzushi.generation :only (create-templater)])
  (:require
    [gaeshi.kuzushi.version :as version])
  (:import
    [java.io File]
    [filecabinet FakeFileSystem Templater Templater$TemplaterLogger]))

(describe "New Command"

  (with fs (FakeFileSystem/installed))

  (it "parses new command"
    (should (:*errors (parse-args)))
    (should= {:name "foo"} (parse-args "foo")))

  (it "should parse the force options"
    (should-not (:force (parse-args "name")))
    (should (:force (parse-args "-f" "name")))
    (should (:force (parse-args "--force" "name"))))

  (context "with generation"

    (with logger
      (proxy [Templater$TemplaterLogger] []
        (say [message])))
    (around [it]
      (with-redefs [create-templater
                (fn [options]
                  (let [templater (Templater. "." "/templates")]
                    (.setLogger templater @logger)
                    templater))]
        (it)))

    (before
      (.createDirectory @fs "/home")
      (.setWorkingDirectory @fs "/home")
      (.createTextFile @fs "/templates/config/env/appengine-web.xml" "appengine: !-APP_NAME-!!-ENV_SUFFIX-!:!-ENVIRONMENT-!")
      (.createTextFile @fs "/templates/config/env/logging.properties" "lumberjacks ho!")
      (.createTextFile @fs "/templates/config/env/repl_init.clj" "repl_init: !-APP_NAME-!-!-ENVIRONMENT-!")
      (.createTextFile @fs "/templates/config/env/web.xml" "web.xml: !-APP_NAME-!")
      (.createTextFile @fs "/templates/public/images/gaeshi.png" "gaeshi")
      (.createTextFile @fs "/templates/public/javascript/default.js" "javascript")
      (.createTextFile @fs "/templates/public/stylesheets/default.css" "css")
      (.createTextFile @fs "/templates/spec/app/core_spec.clj" "core-spec: !-APP_NAME-!")
      (.createTextFile @fs "/templates/src/app/core.clj" "core: !-APP_NAME-!, dir: !-DIR_NAME-!")
      (.createTextFile @fs "/templates/src/app/view/view_helpers.clj" "view-helpers")
      (.createTextFile @fs "/templates/src/app/view/layout.hiccup.clj" "layout")
      (.createTextFile @fs "/templates/src/app/view/index.hiccup.clj" "index")
      (.createTextFile @fs "/templates/src/app/view/not_found.hiccup.clj" "not_found")
      (.createTextFile @fs "/templates/project.clj" "project: !-APP_NAME-!, gaeshi: !-GAESHI_VERSION-!, gaeshi-dev: !-GAESHI_DEV_VERSION-!")
      )

    (context "of default app"
      (before (execute {:name "app"}))

      (it "generates development configuration"
        (should= "appengine: app-development:development" (.readTextFile @fs "/home/app/config/development/appengine-web.xml"))
        (should= "web.xml: app" (.readTextFile @fs "/home/app/config/development/web.xml"))
        (should= "lumberjacks ho!" (.readTextFile @fs "/home/app/config/development/logging.properties"))
        (should= "repl_init: app-development" (.readTextFile @fs "/home/app/config/development/repl_init.clj")))

      (it "generates production configuration"
        (should= "appengine: app:production" (.readTextFile @fs "/home/app/config/production/appengine-web.xml"))
        (should= "web.xml: app" (.readTextFile @fs "/home/app/config/production/web.xml"))
        (should= "lumberjacks ho!" (.readTextFile @fs "/home/app/config/production/logging.properties")))

      (it "generates misc stuff"
        (should= (format "project: app, gaeshi: %s, gaeshi-dev: %s" version/string version/string) (.readTextFile @fs "/home/app/project.clj"))
        (should= true (.exists @fs "/home/app/WEB-INF")))

      (it "generated public dirs"
        (should= "gaeshi" (.readTextFile @fs "/home/app/public/images/gaeshi.png"))
        (should= "javascript" (.readTextFile @fs "/home/app/public/javascript/app.js"))
        (should= "css" (.readTextFile @fs "/home/app/public/stylesheets/app.css")))

      (it "generates default spec"
        (should= "core-spec: app" (.readTextFile @fs "/home/app/spec/app/core_spec.clj")))

      (it "generates default src"
        (should= "core: app, dir: app" (.readTextFile @fs "/home/app/src/app/core.clj"))
        (should= "view-helpers" (.readTextFile @fs "/home/app/src/app/view/view_helpers.clj"))
        (should= "layout" (.readTextFile @fs "/home/app/src/app/view/layout.hiccup.clj"))
        (should= "index" (.readTextFile @fs "/home/app/src/app/view/index.hiccup.clj"))
        (should= "not_found" (.readTextFile @fs "/home/app/src/app/view/not_found.hiccup.clj"))
        (should= true (.exists @fs "/home/app/src/app/controller"))
        (should= true (.exists @fs "/home/app/src/app/model")))
      )

    (it "handles '-' in the app name"
      (execute {:name "foo-bar"})
      (should= (format "project: foo-bar, gaeshi: %s, gaeshi-dev: %s" version/string version/string) (.readTextFile @fs "/home/foo_bar/project.clj"))
      (should= "gaeshi" (.readTextFile @fs "/home/foo_bar/public/images/gaeshi.png"))
      (should= "core-spec: foo-bar" (.readTextFile @fs "/home/foo_bar/spec/foo_bar/core_spec.clj"))
      (should= "core: foo-bar, dir: foo_bar" (.readTextFile @fs "/home/foo_bar/src/foo_bar/core.clj"))
      (should= "appengine: foo-bar:production" (.readTextFile @fs "/home/foo_bar/config/production/appengine-web.xml")))

    (it "handles '_' in the app name"
      (execute {:name "foo_bar"})
      (should= (format "project: foo-bar, gaeshi: %s, gaeshi-dev: %s" version/string version/string) (.readTextFile @fs "/home/foo_bar/project.clj"))
      (should= "gaeshi" (.readTextFile @fs "/home/foo_bar/public/images/gaeshi.png"))
      (should= "core-spec: foo-bar" (.readTextFile @fs "/home/foo_bar/spec/foo_bar/core_spec.clj"))
      (should= "core: foo-bar, dir: foo_bar" (.readTextFile @fs "/home/foo_bar/src/foo_bar/core.clj"))
      (should= "appengine: foo-bar:production" (.readTextFile @fs "/home/foo_bar/config/production/appengine-web.xml")))
    )
  )

(run-specs)

