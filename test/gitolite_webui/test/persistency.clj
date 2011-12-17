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

#_(against-background [(before :facts) 
			   (around :facts )]
    (fact  => (just ))
    (fact 
    	    (p/ssh-pending) => (just '()))) 

#_(fact (p/access-pending) => (just (list {:name "ronen" :repo "play-1" }))
	(against-background 
	  (before :checks 
	    (do 
            (p/persist-repo-request "ronen" "play-0" nil)
	  	(p/persist-repo-request "ronen" "play-1" "bla@bla.com")))))

#_(deftest diff-watcher-test
  (let [result (atom nil)]
    (letfn [(action [a] (reset! result a)) (enrich [a] (assoc a :email "bla"))]
	(p/diff-watcher action enrich nil {} nil {:repo-request {:data #{{:name "ronen"}}}}) 
      (is (= @result nil))
      (p/diff-watcher action enrich nil {} {:repo-request {:data #{{:name "ronen"}}}} {:repo-request {:data #{}}}) 
	(is (= @result '({:name "ronen" :email "bla"}))))))



