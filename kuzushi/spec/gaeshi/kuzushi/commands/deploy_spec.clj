(ns gaeshi.kuzushi.commands.deploy-spec
  (:use
    [speclj.core]
    [gaeshi.kuzushi.spec-helper]
    [gaeshi.kuzushi.commands.deploy]))

(describe "Deploy Command"

  (with-command-help)

  (it "parses no args"
    (should= {:environment "development"} (parse-args)))

  (it "parses the environment"
    (should= "production" (:environment (parse-args "production")))
    (should= "development" (:environment (parse-args "development"))))
  )


