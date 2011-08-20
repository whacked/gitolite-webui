(ns gitolite-webui.test.view
    (:use gitolite-webui.view midje.sweet 
    	    [net.cgrand.enlive-html :only [select attr=]]))

(def email-input [[:input (attr= :id "email")]])

(def email-tag {:tag :input, :attrs {:id "email", :name "email", :type "text"} :content '()})

(fact (select 
        (with-errors upload-form [[:email ["this is a required field"]]]) email-input)   => 
        (contains (assoc-in email-tag [:attrs :class] "error") :gaps-ok :in-any-order))

(fact 
   (select 
    (re-apply-params upload-form {:email "bla@bla"}) email-input) => (contains (assoc-in email-tag [:attrs :value] "bla@bla") :in-any-order))
