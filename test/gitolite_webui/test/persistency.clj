(ns gitolite-webui.test.persistency
    (:require [gitolite-webui.persistency :as p])
    (:use clojure.test midje.sweet))

(defn cleanup [])

(fact (p/ssh-pending) => (just (list {:name "ronen" :email "narkisr@gmail.com" :key "ssh-rsa 1234" }))
	(against-background (before :checks (p/persist-key-request "ronen" "narkisr@gmail.com" "ssh-rsa 1234"))))

(fact (p/ssh-pending) => (just (list {:name "ronen" :repo "play-0" }))
	(against-background (before :checks (p/persist-repo-request "ronen" "play-0"))))


