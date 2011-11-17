(ns gitolite-webui.test.view
    (:use gitolite-webui.view midje.sweet 
    	    [net.cgrand.enlive-html :only [select attr=]]))

(def email-div [[:div (attr= :id "email")]])

(def email-input [[:input (attr= :id "email")]])

(def email-div-tag {:tag :div :attrs {:id "email", :class "clearfix error"} :content '()})

(def email-input-tag  {:tag :input, :attrs {:value "bla@bla", :id "email", :name "email", :type "text"}, :content '()}) 

(defn snip-contents [container] ((:main container))) 

(defn without-contents [output]
  (-> output first (assoc :content '())))

(fact 
  (-> (snip-contents upload-form)
	(with-errors  [[:email ["this is a required field"]]])
	(select email-div)
	(without-contents))   => (contains email-div-tag :gaps-ok :in-any-order))

(fact 
  (without-contents (select 
    (re-apply-params (snip-contents upload-form) {:email "bla@bla"}) email-input)) => (contains (assoc-in email-input-tag [:attrs :value] "bla@bla") :in-any-order))
