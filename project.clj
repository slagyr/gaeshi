(defproject gaeshi "1.0.0-SNAPSHOT"
  :description "FIXME: write"
  :dependencies [[org.clojure/clojure "1.2.0"]
                 [ring/ring-servlet "0.3.5"]
                 [appengine "0.4.3-SNAPSHOT"]
                 [fresh "1.0.1"]]
  :dev-dependencies [[speclj "1.2.0"]
                     [junit "4.8.2"]
                     [com.google.appengine/appengine-api-labs "1.4.0"]
                     [com.google.appengine/appengine-api-stubs "1.4.0"]
                     [com.google.appengine/appengine-local-runtime "1.4.0"]
                     [com.google.appengine/appengine-local-runtime-shared "1.4.0"]
                     [com.google.appengine/appengine-testing "1.4.0"]]
  :test-path "spec/"
  :java-source-path "src/"
  :repositories { "local" "file://m2"}
  )
