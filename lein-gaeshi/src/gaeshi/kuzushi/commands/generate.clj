(ns gaeshi.kuzushi.commands.generate
  (:use
    [joodo.kuzushi.common :only (symbolize)]
    [joodo.kuzushi.generation :only (create-templater add-tokens ->path ->name)]
    [joodo.kuzushi.commands.help :only (usage-for)]
    [joodo.kuzushi.commands.generate :as joodo :only (generate-controller)])
  (:import
    [filecabinet FileSystem Templater]
    [mmargs Arguments]))

(def arg-spec joodo/arg-spec)
(defn parse-args [& args] (apply joodo/parse-args args))

(defn execute
  "Generates files for various components at the specified namespace:
    controller - new controller and spec file"
  [options]
(println "options: " options)
  (let [templater (create-templater options)
        generator (.toLowerCase (:generator options))]
    (cond
      (= "controller" generator) (generate-controller templater options)
      :else (usage-for "generate" [(str "Unknown generator: " generator)]))))

