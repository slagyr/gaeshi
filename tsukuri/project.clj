(defproject gaeshi/tsukuri "0.4.0"
  :description "Development library for Gaeshi, a Clojure framework for Google App Engine apps."
  :repositories {"releases" "http://appengine-magic-mvn.googlecode.com/svn/releases/"}
  :dependencies [[org.clojure/clojure "1.2.1"]
                 [speclj "1.3.1"]
                 [mmargs "1.2.0"]
                 [fresh "1.0.1"]
                 [com.google.appengine/appengine-api-labs "1.4.3"]
                 [com.google.appengine/appengine-api-stubs "1.4.3"]
                 [com.google.appengine/appengine-local-runtime "1.4.3"]
                 [com.google.appengine/appengine-local-runtime-shared "1.4.3"]
                 [com.google.appengine/appengine-testing "1.4.3"]]
  :dev-dependencies [[speclj "1.3.1"]
                     [lein-clojars "0.6.0"]
                     [com.google.appengine/appengine-api-1.0-sdk "1.4.3"]
                     [ring/ring-servlet "0.3.7"]]
  :test-path "spec/"
  :java-source-path "src/")
