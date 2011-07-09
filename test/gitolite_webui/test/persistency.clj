(ns gitolite-webui.test.persistency
    (:require [gitolite-webui.persistency :as p])
    (:use clojure.test midje.sweet))

(defn cleanup [])

(against-background [(after :contents (cleanup))]
    (p/initialize "/tmp/gitolite-db")
    (fact (p/ssh-pending) => (just []))
    (p/persist-key-request ("narkis" "narkisr@gmail.com" "ssh-rsa AAABBBCCC== ronen@bla"))
    (fact (p/ssh-pending) (just [{:user "narkisr" :email "narkisr@gmail.com" :ssh-key "ssh-rsa AAABBBCCC== ronen@bla" :pending "true"}])) 
    )
