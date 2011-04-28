(ns gaeshi.controllers
  (:use
    [compojure.core :only (routing)])
  (:require
    [clojure.string :as str]))

(defn- namespaces-for-parts [root parts]
  (if (seq parts)
    (let [new-root (str root "." (first parts))
          controller-ns (str new-root "-controller")]
      (cons controller-ns (lazy-seq (namespaces-for-parts new-root (rest parts)))))
    []))

(defn namespaces-for-path [root path]
  (let [parts (filter #(not (empty? %)) (str/split path #"[/\.\?]"))]
    (namespaces-for-parts root parts)))

(defn- resolve-controller [ns-name]
  (let [controller-name (last (str/split (name ns-name) #"\."))
        ns-sym (symbol ns-name)]
    (require ns-sym)
    (if-let [controller-ns (find-ns ns-sym)]
      (ns-resolve controller-ns (symbol controller-name)))))

(defn- add-controller [handlers controller]
  (dosync
    (if (not (contains? (set @handlers) controller))
      (alter handlers conj controller))))

(defn- has-duplicate? [values]
  (= (count values) (count (set values))))

(defn- load-controller [root handlers request]
  (let [root-str (name root)
        namespaces (namespaces-for-path root-str (:uri request))
        controller (some resolve-controller namespaces)]
    (when controller
      (add-controller handlers controller))
    controller))

(defn controller-router [root]
  (let [handlers (ref [] :validator has-duplicate?)]
    (fn [request]
      (if-let [response (apply routing request @handlers)]
        response
        (if-let [controller (load-controller root handlers request)]
          (controller request))))))

