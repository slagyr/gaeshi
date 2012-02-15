(use 'joodo.env)

(def environment {
  :joodo-env "!-ENVIRONMENT-!"
  })

(swap! *env* merge environment)