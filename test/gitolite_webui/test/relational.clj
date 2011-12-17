(ns gitolite-webui.test.relational 
    (:use gitolite-webui.relational clojure.test korma.core ))

(defn schema-setup [f]
   (create-schema)
   (f) 
   (drop-schema))

(use-fixtures :each schema-setup)

(deftest acc-request-sanity
   (insert acc-request (values {:NAME "blue boy" :REPO "repo name"}))
   (is (= (select acc-request) [{:ID 1 :NAME "blue boy" :REPO "repo name"}])))

(deftest key-store-sanity
   (let [ssh-key (slurp "test/resources/id_rsa.pub")]
    (insert key-request (values {:NAME "blue boy" :SSH_KEY ssh-key})) 
    (is (= (select key-request) [{:ID 1 :NAME "blue boy" :SSH_KEY ssh-key}]))))
