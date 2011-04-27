(defproject gaeshi/kake "0.4.0"
  :description "Runtime library for Gaeshi, a Clojure framework for Google App Engine apps."
  :repositories {"releases" "http://appengine-magic-mvn.googlecode.com/svn/releases/"}
  :dependencies [[org.clojure/clojure "1.2.1"]
                 [ring/ring-servlet "0.3.7"]
                 [compojure "0.6.2"]
                 [hiccup "0.3.1"]
                 [com.google.appengine/appengine-api-1.0-sdk "1.4.3"]
                 [inflections "0.4.3"]]
  :dev-dependencies [[speclj "1.3.1"]
                     [gaeshi/tsukuri "0.4.0"]
                     [lein-clojars "0.6.0"]]
  :test-path "spec/"
  :java-source-path "src/"
  )
