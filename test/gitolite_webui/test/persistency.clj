(ns gitolite-webui.test.persistency
    (:require [gitolite-webui.persistency :as p])
    (:use clojure.test midje.sweet))

(against-background [(before :checks (p/persist-key-request "ronen" "narkisr@gmail.com" "ssh-rsa 1234"))]
    (fact (p/ssh-pending) => (just (list {:name "ronen" :email "narkisr@gmail.com" :key "ssh-rsa 1234" })))) 

(fact (p/access-pending) => (just (list {:name "ronen" :repo "play-0" }))
	(against-background (before :checks (p/persist-repo-request "ronen" "play-0"))))


