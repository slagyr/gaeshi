(ns gaeshi.kuzushi.commands.new
  (:use
    [joodo.kuzushi.common :only (symbolize)]
    [gaeshi.kuzushi.version :only (gaeshi-version gaeshi-dev-version)]
    [joodo.kuzushi.generation :only (create-templater add-tokens ->path ->ns ->name)])
  (:import
    [filecabinet FileSystem Templater]
    [mmargs Arguments]))

(def arg-spec (Arguments.))
(doto arg-spec
  (.addParameter "name" "The name of the new project.")
  (.addSwitchOption "f" "force" "Overwrite existing files"))

(defn parse-args [& args]
  (symbolize (.parse arg-spec (into-array String args))))

(defn- add-config [options templater env]
  (let [env-suffix (if (= "production" env) "" (str "-" env))
        dir-name (:dir-name options)
        root-ns (:root-ns options)]
    (add-tokens templater "APP_NAME" root-ns "ENV_SUFFIX" env-suffix "ENVIRONMENT" env)
    (.file templater (format "%s/config/%s/appengine-web.xml" dir-name env) "config/env/appengine-web.xml")
    (.file templater (format "%s/config/%s/web.xml" dir-name env) "config/env/web.xml")
    (.file templater (format "%s/config/%s/logging.properties" dir-name env) "config/env/logging.properties")
    (when (= "development" env)
      (.file templater (format "%s/config/%s/repl_init.clj" dir-name env) "config/env/repl_init.clj"))))

(defn- add-misc [options templater]
  (let [dir-name (:dir-name options)
        root-ns (:root-ns options)]
    (add-tokens templater "APP_NAME" root-ns "GAESHI_VERSION" gaeshi-version "GAESHI_DEV_VERSION" gaeshi-dev-version)
    (.file templater (format "%s/project.clj" dir-name) "project.clj")
    (.directory templater (format "%s/WEB-INF" dir-name))))

(defn- add-publics [options templater]
  (let [dir-name (:dir-name options)]
    (.binary templater (format "%s/public/images/gaeshi.png" dir-name) "public/images/gaeshi.png")
    (.file templater (format "%s/public/javascript/%s.js" dir-name dir-name) "public/javascript/default.js")
    (.file templater (format "%s/public/stylesheets/%s.css" dir-name dir-name) "public/stylesheets/default.css")))

(defn- add-default-src [options templater]
  (let [dir-name (:dir-name options)
        root-ns (:root-ns options)]
    (add-tokens templater "APP_NAME" root-ns)
    (add-tokens templater "DIR_NAME" dir-name)
    (.file templater (format "%s/spec/%s/core_spec.clj" dir-name dir-name) "spec/app/core_spec.clj")
    (.file templater (format "%s/src/%s/core.clj" dir-name dir-name) "src/app/core.clj")
    (.directory templater (format "%s/src/%s/controller" dir-name dir-name))
    (.directory templater (format "%s/src/%s/model" dir-name dir-name))
    (.file templater (format "%s/src/%s/view/view_helpers.clj" dir-name dir-name) "src/app/view/view_helpers.clj")
    (.file templater (format "%s/src/%s/view/layout.hiccup.clj" dir-name dir-name) "src/app/view/layout.hiccup.clj")
    (.file templater (format "%s/src/%s/view/index.hiccup.clj" dir-name dir-name) "src/app/view/index.hiccup.clj")
    (.file templater (format "%s/src/%s/view/not_found.hiccup.clj" dir-name dir-name) "src/app/view/not_found.hiccup.clj")))


(defn execute
  "Creates all the needed files for new Gaeshi project."
  [options]
  (let [options (assoc options :dir-name (->path (:name options)))
        options (assoc options :root-ns (->ns (:name options)))
        templater (create-templater options)]
    (.createDirectory (FileSystem/instance) (:name options))
    (add-misc options templater)
    (add-config options templater "development")
    (add-config options templater "production")
    (add-publics options templater)
    (add-default-src options templater)))

