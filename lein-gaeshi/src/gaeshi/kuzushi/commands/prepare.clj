(ns gaeshi.kuzushi.commands.prepare
  "Build a deployable directory structure of the app"
  (:require [leiningen.jar :refer [jar get-jar-filename write-jar]]
            [clojure.java.io :refer [file]]
            [clojure.string :as str :refer [join]]
            [joodo.kuzushi.common :refer [symbolize with-lein-project *project*]]
            [leiningen.core.classpath :refer [get-classpath]]
            [lancet.core :as lancet])
  (:import [java.io File]
           [mmargs Arguments]))

(def arg-spec (Arguments.))
(doto arg-spec
  (.addOptionalParameter "environment" "Specifies which environment to prepare. (default: development)"))

(def default-options {:environment "development"})

(defn parse-args [& args]
  (let [options (symbolize (.parse arg-spec (into-array String args)))]
    (merge default-options options)))

(defn- clean-classes [project]
  (lancet/delete {:dir "war/WEB-INF/classes"}))

(defn- create-app-jar [project]
  (let [core-namespace (or (:gaeshi-core-namespace project) (str (:name project) ".core"))
        project (assoc project :aot :all ) ;[core-namespace])
        project (assoc project :keep-non-project-classes true)
        project (assoc project :omit-source true)]
    (jar project)))

(defn- clean-libs [project]
  (lancet/delete {:dir "war/WEB-INF/lib"})
  (lancet/mkdir {:dir "war/WEB-INF/lib"}))

(defn- prepare-app-jar [project]
  (create-app-jar project)
  (clean-libs project)
  (lancet/copy {:file (get-jar-filename project) :todir "war/WEB-INF/lib"}))

(defn- prepare-views-jar [project]
  (let [default-jar-file (File. (get-jar-filename project))
        views-jar-name (str/replace (.getName default-jar-file) #"\.jar" "_views.jar")
        views-jar-path (.getPath (File. (.getParent default-jar-file) views-jar-name))
        source-path (or (:source-path project) (first (:source-paths project)))]
    (lancet/jar {:jarfile views-jar-path :basedir source-path :includes "**/*.hiccup*"})
    (lancet/copy {:file views-jar-path :todir "war/WEB-INF/lib"})))

;(defn- prepare-libs [project]
;  (let [lib-dir (:library-path project)
;        dev-lib-dir (.getPath (file lib-dir "dev"))]
;    (lancet/copy {:todir "war/WEB-INF/lib"}
;      (lancet/fileset {:dir lib-dir :includes "*" :excludes "dev"}))))

(defn- prepare-libs [project]
  (let [classpaths (get-classpath *project*)
        jars (filter #(.endsWith % ".jar") classpaths)]
    (doseq [jar jars]
      (lancet/copy {:file jar :todir "war/WEB-INF/lib"}))))

(defn- clean-public [project]
  (lancet/delete {:dir "war/public"})
  (lancet/mkdir {:dir "war/public"}))

(defn- prepare-public [project]
  (clean-public project)
  (lancet/copy {:todir "war/public"}
    (lancet/fileset {:dir "public" :includes "**/*"})))

(defn- prepare-config [project env]
  (lancet/delete {} (lancet/fileset {:dir "war/WEB-INF" :includes "*.xml,*.properties"}))
  (lancet/copy {:todir "war/WEB-INF"}
    (lancet/fileset {:dir (str "config/" env) :includes "**/*"})))

(defn prepare [project options]
  (let [env (:environment options)]
    (println "Gaeshi: preparing the" env "environment for deployment")
    (clean-classes project)
    (prepare-app-jar project)
    (prepare-views-jar project)
    (prepare-libs project)
    (prepare-public project)
    (prepare-config project env)
    (println "Gaeshi:" env "environment prepared for deployment.")))

(defn execute
  "Build a deployable directory structure of the app"
  [options]
  (with-lein-project
    (prepare *project* options)))


