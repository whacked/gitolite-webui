(ns gitolite-webui.test.persistency
    (:require [gitolite-webui.persistency :as p] [clojure.contrib.datalog.database :as dblog]) 
    (:use clojure.test midje.sweet clojure.contrib.trace))

(against-background [(before :facts (p/persist-key-request "ronen" "narkisr@gmail.com" "ssh-rsa 1234")) 
			   (around :facts (let [typed-req (with-meta {:name "ronen" :key "ssh-rsa 1234" } {:type :key-request}) ] ?form))]
    (fact (p/ssh-pending) => (just (list typed-req)))
    (fact (p/clear-request typed-req)
    	    (p/ssh-pending) => (just '()))) 

(fact (p/access-pending) => (just (list {:name "ronen" :repo "play-1" }))
	(against-background 
	  (before :checks 
	    (do 
            (p/persist-repo-request "ronen" "play-0" nil)
	  	(p/persist-repo-request "ronen" "play-1" "bla@bla.com")))))

(deftest diff-watcher-test
  (let [result (atom nil)]
    (letfn [(action [a] (reset! result a)) (enrich [a] (assoc a :email "bla"))]
	(p/diff-watcher action enrich nil {} nil {:repo-request {:data #{{:name "ronen"}}}}) 
      (is (= @result nil))
      (p/diff-watcher action enrich nil {} {:repo-request {:data #{{:name "ronen"}}}} {:repo-request {:data #{}}}) 
	(is (= @result '({:name "ronen" :email "bla"}))))))

