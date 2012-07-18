(let [config (load-file "../config.clj")
      dev-deps [['speclj (:speclj-version config)]
                ['filecabinet "1.0.4"]]]

(defproject gaeshi/lein-gaeshi (:version config)
  :description "Leiningen Plugin for Gaeshi, a Clojure framework for Google App Engine apps."
  :license {:name "The MIT License"
            :url "file://LICENSE"
            :distribution :repo
            :comments "Copyright © 2011-2012 Micah Martin All Rights Reserved."}
  :dependencies [[joodo/lein-joodo ~(:joodo-version config)]
                 [leiningen "2.0.0-preview7"]
                 [lancet "1.0.1"]]
  :profiles {:dev {:dependencies ~dev-deps}}
  :plugins ~dev-deps
  :test-path "spec/"
  :test-paths ["spec/"]
  :shell-wrapper {:main gaeshi.kuzushi.main
                  :bin "bin/gaeshi"}
  :resources-path "resources/"
  ))
