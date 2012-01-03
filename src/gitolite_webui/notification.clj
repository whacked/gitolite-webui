(ns gitolite-webui.notification
    (:use 
      gitolite-webui.config
      clojure.contrib.strint
      clojure.contrib.logging
      [trammel.core :only (with-constraints contract)])
    (:import 
     (org.apache.commons.mail SimpleEmail DefaultAuthenticator EmailException)))

(defmacro non-nil-params [fn]
  "Checks that all input parameters to fn are not nil"
  `(every? (comp not nil?) ~(first (:arglists (meta (resolve fn))))))

(defmacro keys-not-empty [map & keys]
  "Checks that all given keys are not empty in map" 
  `(every? (comp not empty?) ((juxt ~@keys) ~map))
  )


(defn notify-user [to subject body] 
       {:pre [(non-nil-params notify-user) (keys-not-empty (:email @config) :host :from :user :pass)]}
	 (let [{:keys [from user pass host port ssl]} (:email @config)]
	   (try
	     (do
	       (doto (SimpleEmail.)
			    (.setHostName host) 
			    (.setSmtpPort port)
			    (.setAuthenticator (DefaultAuthenticator. user  pass))
			    (.setTLS ssl)
			    (.setFrom from)
			    (.setSubject subject)
			    (.setMsg body)
			    (.addTo to)
			    (.send)) 
		 (info (<< "Email send to ~{to}")))
	     (catch EmailException e (error (str e " while send email to " to))) 
	     ) 
	   ))

(defn email-request [{:keys [email] :as req} ]
  (condp = (type req) 
   :repo-request (notify-user email "Your repository access request has been approved" (<< "Verify that you can access it by cloning ~(:repo req) repository.")) 
   :key-request (notify-user email "Your key request in gitolite was approved" (<< "Please verify that your public key matches ~(:key req)"))
    )
  )

(defn email-approved [approved]
  (doall (map deref (for [req approved] (future (email-request req))))))



