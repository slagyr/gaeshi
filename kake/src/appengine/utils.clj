(ns #^{:author "Roman Scherer"
       :doc "Utility functions used by other namespaces."}
  appengine.utils
  (:use [clojure.contrib.seq :only (includes?)]))

(defn compact [seq]
  (remove nil? seq))

(defn constructor-types
  "Returns the parameter types of all constuctors."
  [class] (map #(.getParameterTypes %) (.getConstructors class)))

(defn min-constructor-args
  "Returns the minimum number of constuctor arguments."
  [class] (apply min (map count (constructor-types class))))

(defn flat-seq [arg]
  (flatten (seq arg)))

(defn map-keys
  "Returns a lazy sequence consisting of the result of applying f to
  the keys of coll."
  [coll f] (zipmap (map f (keys coll)) (vals coll)))

(defn map-keyword [coll]
  "Returns a lazy sequence consisting of the result of applying
  #'keyword to the keys of coll."
  (map-keys coll #'keyword))

(defn keyword->string
  "Returns a string by calling str on the keyword and removing the
double colon at the beginning. If called with a string as argument the
string will be returned without modification.

Examples:

  (keyword->string :iso-3166-alpha-2)
  ; => \"iso-3166-alpha-2\"

  (keyword->string \"iso-3166-alpha-2\")
  ; => \"iso-3166-alpha-2\"
"
  [keyword]
  (let [string (str keyword)]
    (if (= (first string) \:) (apply str (rest string)) string)))

(defn immigrate-symbols
  "Create a public var in this namespace for each public var in the
namespace that is included in the symbols list. The created vars have
the same name, root binding, and metadata as the original except that
their :ns metadata value is this namespace."
  [namespace & symbols]
  (require namespace)  
  (doseq [[sym var] (ns-publics namespace)]    
    (if (includes? symbols sym)
      (let [sym (with-meta sym (assoc (meta var) :ns *ns*))]
        (if (.hasRoot var)
          (intern *ns* sym (.getRoot var))
          (intern *ns* sym))))))

(defn stringify
  "Returns a stringified version of the given argument. Keywords are
converted with the keyword->string function, all other by calling str
on the argument.

Examples:

  (stringify \"iso-3166-alpha-2\")
  ; => \"iso-3166-alpha-2\"

  (stringify :iso-3166-alpha-2)
  ; => \"iso-3166-alpha-2\"

  (stringify 'iso-3166-alpha-2)
  ; => \"iso-3166-alpha-2\"
"
  [arg] (if (keyword? arg) (keyword->string arg) (str arg)))
