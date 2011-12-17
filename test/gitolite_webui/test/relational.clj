(ns gitolite-webui.test.relational 
    (:use gitolite-webui.relational clojure.test korma.core ))

(deftest schema-creation
   (create-schema)
   (insert acc-request (values {:ID  1 :NAME "blue boy" :REPO "repo name"}))
   (is (= (select acc-request) [{:ID 1 :NAME "blue boy" :REPO "repo name"}])) 
   (drop-schema)
   )
