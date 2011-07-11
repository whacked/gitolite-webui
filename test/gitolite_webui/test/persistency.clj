(ns gitolite-webui.test.persistency
    (:require [gitolite-webui.persistency :as p])
    (:use clojure.test midje.sweet))

(defn cleanup [])



(fact (p/ssh-pending) => (just []))

(fact (p/ssh-pending) => (just (list {:name "ronen" :email "narkisr@gmail.com" :key "ssh-rsa 1234" }))
	(against-background (before :checks (p/persist-key-request "ronen" "narkisr@gmail.com" "ssh-rsa 1234"))))


(against-background [(after :contents (cleanup))]
			  (p/initialize "/tmp/gitolite-db")
			  #_(p/persist-key-request ("narkis" "narkisr@gmail.com" "ssh-rsa AAABBBCCC== ronen@bla"))
			  #_(fact (p/ssh-pending) (just [{:user "narkisr" :email "narkisr@gmail.com" :ssh-key "ssh-rsa AAABBBCCC== ronen@bla" :pending "true"}])) 
			  )
