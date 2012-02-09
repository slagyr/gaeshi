(ns gaeshi.kuzushi.commands.prepare-spec
  (:use
    [speclj.core]
    [gaeshi.kuzushi.spec-helper]
    [gaeshi.kuzushi.commands.prepare]))

(describe "Prepare Command"

  (with-command-help)

  (it "parses no args"
    (should= {:environment "development"} (parse-args)))

  (it "parses the environment"
    (should= "production" (:environment (parse-args "production")))
    (should= "development" (:environment (parse-args "development"))))
  )

