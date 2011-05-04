(ns gaeshi.kuzushi.commands.version-spec
  (:use
    [speclj.core]
    [gaeshi.kuzushi.commands.version]
    [gaeshi.kuzushi.spec-helper]
    [gaeshi.kuzushi.core :only (run)]
    [gaeshi.kuzushi.common :only (endl)]))

(describe "Version Comamnd"

  (with-command-help)

  (it "parses no args"
    (should= {} (parse-args)))

  (it "handles the --version command"
    (should= 0 (run "--version"))
    (should= (str "gaeshi/kuzushi " gaeshi.kuzushi.version/string endl) (to-s @output)))

  )
