(ns gaeshi.kuzushi.commands.help
  (:require
    [joodo.kuzushi.commands.help :as help]))

(def arg-spec help/arg-spec)
(defn parse-args [& args] (apply help/parse-args args))

(defn execute
  "Prints help message for commands: gaeshi help <command>"
  [options] (help/execute options))

