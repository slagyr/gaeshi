(ns leiningen.prepare
  "Build a deployable directory structure of the app"
  (:use
    [leiningen.jar :only (jar get-jar-filename write-jar)]
    [clojure.java.io :only (file)]
    [clojure.string :as str :only (join)])
  (:require
    [leiningen.util.file]
    [lancet.core :as lancet])
  (:import
    [java.io File]))

(defn- clean-classes [project]
  (lancet/delete {:dir "war/WEB-INF/classes"}))

(defn- create-app-jar [project]
  (let [core-namespace (or (:gaeshi-core-namespace project) (str (:name project) ".core"))
        project (assoc project :aot [core-namespace])
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
        views-jar-path (.getPath (File. (.getParent default-jar-file) views-jar-name))]
    (lancet/jar {:jarfile views-jar-path :basedir (:source-path project) :includes "**/*.hiccup.clj"})
    (lancet/copy {:file views-jar-path :todir "war/WEB-INF/lib"})))

(defn- prepare-libs [project]
  (let [lib-dir (:library-path project)
        dev-lib-dir (.getPath (file lib-dir "dev"))]
    (lancet/copy {:todir "war/WEB-INF/lib"}
      (lancet/fileset {:dir lib-dir :includes "*" :excludes "dev"}))
    (lancet/copy {:todir "war/WEB-INF/lib"}
      (lancet/fileset {:dir dev-lib-dir :includes (join ","
        ["appengine-api-1.0-sdk*"
         "commons-io*"
         "commons-codec*"
         "commons-fileupload*"
         "compojure*"
         "gaeshi*"
         "hiccup*"
         "ring-core*"
         "ring-servlet*"
         "servlet-api*"])}))))

(defn clean-public [project]
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

(defn prepare [project & args]
  (let [env (or (first args) "development")]
    (clean-classes project)
    (prepare-app-jar project)
    (prepare-views-jar project)
    (prepare-libs project)
    (prepare-public project)
    (prepare-config project env)
    ))


; TODO
; * 2 different gaeshi libraries? 1 lien plugin to generate project/provide lein tasks, and 1 to bring in all deps.

