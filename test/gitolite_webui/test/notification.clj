(ns gitolite-webui.test.notification
    (:use 
       clojure.test midje.sweet 
       gitolite-webui.notification 
     ))

(fact 
  (notify-user-constrained "bla@bla" "subjet" "body" {:email {:user nil :pass "xyz" :host "" :port 5 :ssl false}}) => (throws java.lang.AssertionError)
  (notify-user-constrained nil "subjet" "body" {:email {:user "bla" :pass "xyz" :host "google" :port 5 :ssl false}}) => (throws java.lang.AssertionError)
  )
