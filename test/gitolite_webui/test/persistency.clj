(ns gitolite-webui.test.persistency
    (:use korma.core
          gitolite-webui.persistency 
          clojure.test midje.sweet 
          clojure.contrib.trace))

(defn schema-setup [f]
   (create-schema)
   (f) 
   (drop-schema))

(use-fixtures :each schema-setup)
(use-fixtures :once initialize-db)

(defn assert-row-and-type [rows row rtype]
    (is (= rows [row]))
    (is (= (-> rows first type) rtype)))

(deftest repo-request-sanity
   (insert repo-request (values {:name "blue boy" :repo "repo name"}))
   (assert-row-and-type
      (select repo-request) {:name "blue boy" :repo "repo name"} :repo-request)
   #_(insert repo-request (values {:name "blue boy" :repo "repo name"}))
   )

(deftest key-store-sanity
   (let [ssh-key (slurp "test/resources/id_rsa.pub")]
     (insert key-request (values {:name "blue boy" :key ssh-key})) 
     (assert-row-and-type
       (select key-request) {:name "blue boy" :key ssh-key} :key-request)))

(deftest contact-sanity
    (insert contact (values {:name "blue boy" :email "bla@bla.com"})) 
    (is (= (select contact) [{:name "blue boy" :email "bla@bla.com"}])))

(deftest ssh-pending-interactions
  (let [typed-req (with-meta {:name "ronen" :key "ssh-rsa 1234" } {:type :key-request})]
   (persist-key-request "ronen" "narkisr@gmail.com" "ssh-rsa 1234") 
   (is (= (ssh-pending) [typed-req])) 
   (clear-request typed-req))
   (is (= (ssh-pending) '())))

(deftest multipe-repo-requests-for-user
  (persist-repo-request "ronen" "play-0" nil)
  (persist-repo-request "ronen" "play-1" "bla@bla.com")
  (is (= (access-pending) [{:name "ronen" :repo "play-0" } {:name "ronen" :repo "play-1" } ])))
