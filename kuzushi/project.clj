(defproject gaeshi/kuzushi "0.5.1"
  :description "Leiningen Plugin for Gaeshi, a Clojure framework for Google App Engine apps."
  :license {:name "The MIT License"
            :url "file://LICENSE"
            :distribution :repo
            :comments "Copyright © 2011 Micah Martin All Rights Reserved."}
  :dependencies [[org.clojure/clojure "1.2.1"]
                 [filecabinet "1.0.3"]
                 [mmargs "1.2.0"]]
  :dev-dependencies [[speclj "1.4.0"]
                     [lein-clojars "0.6.0"]]
  :test-path "spec/"
  :shell-wrapper {:main gaeshi.kuzushi.core
                  :bin "bin/gaeshi"}
  :resources-path "resources/"
;  :eval-in-leiningen true
  )
