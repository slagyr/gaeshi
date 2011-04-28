(ns gaeshi.controllers-spec
  (:use
    [speclj.core]
    [gaeshi.controllers]))

(describe "Controllers"

  (it "determines the namespaces for a request"
    (should= [] (namespaces-for-path "root" "/"))
    (should= ["root.one-controller"] (namespaces-for-path "root" "/one"))
    (should= ["root.one-controller" "root.one.two-controller"] (namespaces-for-path "root" "/one/two"))
    (should= ["root.one-controller" "root.one.two-controller" "root.one.two.three-controller"] (namespaces-for-path "root" "/one/two/three"))
    (should= ["root.one-controller" "root.one.two-controller" "root.one.two.blah-controller"] (namespaces-for-path "root" "/one/two.blah")))

  (it "controller-router creates a ring-handler will dynamically load controllers"
    (let [required-ns (atom nil)
          resolved-var (atom nil)]
      (binding [require (fn [name] (reset! required-ns name))
                find-ns (fn [name] :fake-ns)
                ns-resolve (fn [ns var] (reset! resolved-var var) (fn [request] :fake-response))]
        (let [router (controller-router 'root)
              response (router {:uri "/one"})]
          (should= :fake-response response)
          (should= 'root.one-controller @required-ns)
          (should= 'one-controller @resolved-var)))))

  (it "controller-router reuses previously loaded controllers"
    (let [required-ns (atom nil)
          resolved-var (atom nil)
          router (controller-router 'root)]
      (binding [require (fn [name] (reset! required-ns name))
                find-ns (fn [name] :fake-ns)
                ns-resolve (fn [ns var] (reset! resolved-var var) (fn [request] :fake-response))]
          (should= :fake-response (router {:uri "/one"})))
      (should= :fake-response (router {:uri "/one"}))))
  )
