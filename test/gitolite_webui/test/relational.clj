(ns gitolite-webui.test.relational 
    (:use gitolite-webui.relational clojure.test korma.core ))

(defn schema-setup [f]
   (create-schema)
   (f) 
   (drop-schema))

(use-fixtures :each schema-setup)

(defn assert-row-and-type [rows row rtype]
    (is (= rows [row]))
    (is (= (-> rows first type) rtype)))

(deftest acc-request-sanity
   (insert acc-request (values {:NAME "blue boy" :REPO "repo name"}))
   (assert-row-and-type
      (select acc-request) {:ID 1 :NAME "blue boy" :REPO "repo name"} :repo-request))

(deftest key-store-sanity
   (let [ssh-key (slurp "test/resources/id_rsa.pub")]
     (insert key-request (values {:NAME "blue boy" :SSH_KEY ssh-key})) 
     (assert-row-and-type
       (select key-request) {:ID 1 :NAME "blue boy" :SSH_KEY ssh-key} :key-request)))

(deftest contact-sanity
    (insert contact (values {:NAME "blue boy" :EMAIL "bla@bla.com"})) 
    (is (= (select contact) [{:NAME "blue boy" :EMAIL "bla@bla.com"}])))
