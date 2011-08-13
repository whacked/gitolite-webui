(ns gitolite-webui.test.persistency
    (:require [gitolite-webui.persistency :as p])
    (:use clojure.test midje.sweet))

(against-background [(before :facts (p/persist-key-request "ronen" "narkisr@gmail.com" "ssh-rsa 1234")) 
			   (around :facts (let [typed-req (with-meta {:name "ronen" :email "narkisr@gmail.com" :key "ssh-rsa 1234" } {:type :key-request}) ] ?form))]
    (fact (p/ssh-pending) => (just (list typed-req)))
    (fact (p/clear-request typed-req)
    	    (p/ssh-pending) => (just '()))) 

(fact (p/access-pending) => (just (list {:name "ronen" :repo "play-0" }))
	(against-background (before :checks (p/persist-repo-request "ronen" "play-0"))))


