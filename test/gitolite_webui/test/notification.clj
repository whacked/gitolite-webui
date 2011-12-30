(ns gitolite-webui.test.notification
    (:use 
       clojure.test 
       gitolite-webui.config
       gitolite-webui.notification 
     ))

(deftest notify-user-assertion
  (swap! config assoc-in [:email :user] nil)
  (is (thrown? java.lang.AssertionError (notify-user "bla@bla" "subjet" "body"))) 
  (swap! config assoc-in [:email :user] "bla")
  (is (thrown? java.lang.AssertionError (notify-user nil "subjet" "body"))) 
  )
