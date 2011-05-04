(require 'gaeshi.kuzushi.version)

(defproject gaeshi/kuzushi gaeshi.kuzushi.version/string
  :description "Leiningen Plugin for Gaeshi, a Clojure framework for Google App Engine apps."
  :dependencies [[org.clojure/clojure "1.2.1"]
                 [filecabinet "1.0.1"]
                 [mmargs "1.2.0"]]
  :dev-dependencies [[speclj "1.3.1"]
                     [lein-clojars "0.6.0"]]
  :test-path "spec/"
  :shell-wrapper {:main gaeshi.kuzushi.core
                  :bin "bin/gaeshi"})
