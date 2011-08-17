(ns gitolite-webui.test.view
    
    (:use gitolite-webui.view midje.sweet 
    	    [net.cgrand.enlive-html :only [select attr=]]
    	    ) )

(fact 
      (select (with-errors upload-form [[:email ["this is a required field"]]]) [[ :input (attr= :id "email")]])   => 
	(contains {:tag :input, :attrs {:class "error", :id "email", :name "email", :type "text"}, :content '()} :gaps-ok :in-any-order))
