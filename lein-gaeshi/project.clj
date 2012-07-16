(def config (load-file "../config.clj"))

(defproject gaeshi/lein-gaeshi (:version config)
  :description "Leiningen Plugin for Gaeshi, a Clojure framework for Google App Engine apps."
  :license {:name "The MIT License"
            :url "file://LICENSE"
            :distribution :repo
            :comments "Copyright © 2011-2012 Micah Martin All Rights Reserved."}
  :dependencies [[joodo/lein-joodo ~(:joodo-version config)]]
  :dev-dependencies [[speclj ~(:speclj-version config)]
                     [filecabinet "1.0.4"]]
  :test-path "spec/"
  :shell-wrapper {:main gaeshi.kuzushi.main
                  :bin "bin/gaeshi"}
  :resources-path "resources/"
  :extra-classpath-dirs ["leiningen-1.7.0-standalone.jar"]
  )
