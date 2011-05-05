(ns gaeshi.kuzushi.commands.help
  (:use
    [gaeshi.kuzushi.common :only (exit symbolize load-var)])
  (:require
    [clojure.string :as str])
  (:import
    [mmargs Arguments]
    [filecabinet FileSystem]))

(def main-arg-spec (atom nil))

(def arg-spec (Arguments.))
(doto arg-spec
  (.addOptionalParameter "command" "Get help for this command"))

(defn parse-args [& args]
  (symbolize (.parse arg-spec (into-array String args))))

(defn find-resource [& names]
  (some
    (fn [name] (.getResource (clojure.lang.RT/baseLoader) name))
    names))

(def ns-regex #"^(.*)(?:(?:\.clj)|(?:__init\.class))$")

(defn extract-ns-from-filename [filename]
  (if-let [groups (first (re-seq ns-regex filename))]
    (second groups)
    nil))

(defn all-commands []
  (let [help-src-url (find-resource "gaeshi/kuzushi/commands/help.clj" "gaeshi/kuzushi/commands/help__init.class")
        commands-dir (.parentPath (FileSystem/instance) (.toString help-src-url))
        files (.fileListing (FileSystem/instance) commands-dir)]
    (sort (filter identity (map extract-ns-from-filename files)))))

(defn- docstring-for [command]
  (let [exec-fn (load-var (symbol (str "gaeshi.kuzushi.commands." command)) 'execute)]
    (:doc (meta exec-fn))))

(defn- print-commands []
  (println "  Commands:")
  (let [commands (all-commands)
        docstrings (map docstring-for commands)
        command-ary (into-array commands)
        doc-ary (into-array docstrings)]
    (println (Arguments/tabularize command-ary doc-ary))))

(defn usage [errors]
  (if (seq errors)
    (do
      (println "ERROR!!!")
      (doseq [error (seq errors)]
        (println error))))
  (println)
  (println "Usage: gaeshi" (.argString @main-arg-spec) "[command options]")
  (println)
  (println (.parametersString @main-arg-spec))
  (println (.optionsString @main-arg-spec))
  (print-commands)
  (if (seq errors)
    (exit -1)
    (exit 0)))

(defn usage-for [command]
  )

(defn execute
  "Prints help message for commands: gaeshi help <command>"
  [options]
  (if-let [command (:command options)]
    (usage-for command)
    (usage nil)))