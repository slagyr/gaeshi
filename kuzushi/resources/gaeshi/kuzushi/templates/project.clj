(defproject !-APP_NAME-! "0.0.1"
  :description "A website deployable to AppEngine"
  :dependencies [[org.clojure/clojure "1.2.1"]
                 [gaeshi/kake "0.4.2"]]
  :dev-dependencies [[gaeshi/tsukuri "0.4.2"]
                     [speclj "1.3.1"]]
  :test-path "spec/"
  :java-source-path "src/"
  :repl-init-script "config/development/repl_init.clj"
  :gaeshi-core-namespace !-APP_NAME-!.core)