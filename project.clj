(defproject gaeshi "0.3.3"
  :description "Web Framework For Google App Engine"
  :repositories {"releases" "http://appengine-magic-mvn.googlecode.com/svn/releases/"}
  :dependencies [[org.clojure/clojure "1.2.1"]
                 [ring/ring-servlet "0.3.7"]
                 [compojure "0.6.2"]
                 [speclj "1.3.1"]
                 [fresh "1.0.1"]
                 [hiccup "0.3.1"]
                 [com.google.appengine/appengine-api-1.0-sdk "1.4.3"]
                 [inflections "0.4-SNAPSHOT"]]
  :dev-dependencies [[speclj "1.3.1"]
                     [junit "4.8.2"]
                     [lein-clojars "0.6.0"]
                     [com.google.appengine/appengine-api-labs "1.4.3"]
                     [com.google.appengine/appengine-api-stubs "1.4.3"]
                     [com.google.appengine/appengine-local-runtime "1.4.3"]
                     [com.google.appengine/appengine-local-runtime-shared "1.4.3"]
                     [com.google.appengine/appengine-testing "1.4.3"]]
  :test-path "spec/"
  :java-source-path "src/"
  :aot [gaeshi.spec-helpers.datastore appengine.datastore]
  )
