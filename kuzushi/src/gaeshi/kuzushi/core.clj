(ns gaeshi.kuzushi.core
  (:use
    [gaeshi.kuzushi.common :only (exit symbolize)]
    [gaeshi.kuzushi.commands.help :only (usage)])
  (:require
    [gaeshi.kuzushi.version])
  (:import
    [mmargs Arguments]))

(def arg-spec (Arguments.))
(doto arg-spec
  (.addParameter "command" "The name of the command to execute. Use --help for a listing of command.")
  (.addSwitchOption "v" "version" "Shows the current gaeshi/kuzushi version.")
  (.addSwitchOption "h" "help" "You're looking at it.")
  )

(defn- resolve-aliases [options]
  (cond
     (:help options) (recur (dissoc (assoc options :command "help") :help))
     (:version options) (recur (dissoc (assoc options :command "version") :version))
    :else options))

(defn parse-args [& args]
  (let [parse-result (.parse arg-spec (into-array String args))
        options (symbolize parse-result)
        options (resolve-aliases options)]
    (if-let [command (:command options)]
      options
      (usage (:*errors options)))))

(defn- load-var [ns-sym var-sym]
  (try
    (require ns-sym)
    (let [ns (the-ns ns-sym)]
      (ns-resolve ns var-sym))
    (catch Exception e
      (.printStackTrace e)
      nil)))

(defn run-command [options]
  (try
    (let [command-ns-sym (symbol (str "gaeshi.kuzushi.commands." (:command options)))
          exec-fn (load-var command-ns-sym 'execute)]
      (if exec-fn
        (exec-fn options)
        (throw (Exception. (str "Can't find command: " (:command options))))))
    (catch Exception e
      (.printStackTrace e)
      (exit -1))))

(defn run [& args]
  (let [options (apply parse-args args)]
    (run-command options)))

(defn -main [& args]
  (apply run args))
