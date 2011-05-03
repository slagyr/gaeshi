(ns leiningen.generate
  (:import
    [filecabinet FileSystem Templater]))

(defn create-templater [project]
  (let [destination (:root project)
        templates-marker (.getFile (.getResource (clojure.lang.RT/baseLoader) "gaeshi/tsukuri/templates/marker.txt"))
        source (.parentPath (FileSystem/instance) templates-marker)]
    (Templater. destination source)))

(defn add-tokens [templater & kvargs]
  (let [tokens (apply hash-map kvargs)]
    (doseq [[token value] tokens]
      (.addToken templater token value))))

(defn- add-config [project templater env]
  (let [env-suffix (if (= "production" env) "" (str "-" env))]
    (add-tokens templater "APP_NAME" (:name project) "ENV_SUFFIX" env-suffix "ENVIRONMENT" env)
    (.file templater (format "config/%s/appengine-web.xml" env) "config/env/appengine-web.xml")
    (.file templater (format "config/%s/web.xml" env) "config/env/web.xml")
    (.file templater (format "config/%s/logging.properties" env) "config/env/logging.properties")
    (when (= "development" env)
      (.file templater (format "config/%s/repl_init.clj" env) "config/env/repl_init.clj"))))

(defn- add-publics [project templater]
  (.directory templater "public/images")
  (.file templater (format "public/javascript/%s.js" (:name project)) "public/javascript/default.js")
  (.file templater (format "public/stylesheets/%s.css" (:name project)) "public/stylesheets/default.css"))

(defn- add-default-src [project templater]
  (add-tokens templater "APP_NAME" (:name project))
  (.file templater (format "spec/%s/core_spec.clj" (:name project)) "spec/app/core_spec.clj")
  (.file templater (format "src/%s/core.clj" (:name project)) "src/app/core.clj")
  (.directory templater (format "src/%s/controller" (:name project)))
  (.directory templater (format "src/%s/model" (:name project)))
  (.file templater (format "src/%s/view/view_helpers.clj" (:name project)) "src/app/view/view_helpers.clj")
  (.file templater (format "src/%s/view/layout.hiccup.clj" (:name project)) "src/app/view/layout.hiccup.clj")
  (.file templater (format "src/%s/view/index.hiccup.clj" (:name project)) "src/app/view/index.hiccup.clj")
  (.file templater (format "src/%s/view/not_found.hiccup.clj" (:name project)) "src/app/view/not_found.hiccup.clj")
  )

(defn generate [project]
  (let [project (assoc project :name (.toLowerCase (:name project)))
        templater (create-templater project)]
    (add-config project templater "development")
    (add-config project templater "production")
    (add-publics project templater)
    (add-default-src project templater)
    ))
