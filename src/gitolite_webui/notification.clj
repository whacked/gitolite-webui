(ns gitolite-webui.notification
    (:use 
      gitolite-webui.trammel-checks
      gitolite-webui.config
      clojure.contrib.strint
      clojure.contrib.logging
      [trammel.core :only (with-constraints contract)])
    (:import 
     (org.apache.commons.mail SimpleEmail DefaultAuthenticator EmailException)))


(defn- notify-user [to subject body config]
	 (let [{:keys [user pass host port ssl]} (:email config)]
	   (try
	     (do (doto (SimpleEmail.)
			    (.setHostName host) 
			    (.setSmtpPort port)
			    (.setAuthenticator (DefaultAuthenticator. user  pass))
			    (.setTLS ssl)
			    (.setFrom "gookup@gmail.com")
			    (.setSubject subject)
			    (.setMsg body)
			    (.addTo to)
			    (.send)) 
		 (info (<< "Email send to ~{to}")))
	     (catch EmailException e (error (str e to))) 
	     ) 
	   ))

(def notify-user-contract 
     (contract notify-user-constraints 
		   "Defines constraints for notify-user"
		   [to subject body config] 
		   [(non-nil-params notify-user) 
		    (keys-not-empty (:email config) :user :pass :host)
		    ]))

(def notify-user-constrained (with-constraints notify-user notify-user-contract))

(defmulti email-request keys)
(defmethod email-request '(:email :name :repo) [{:keys [email] :as req} ]
 (notify-user-constrained email "Your repository access request has been approved" (<< "Verify that you can access it by cloning ~(:repo req) repository.") @config) 
  )
(defmethod email-request '(:email :name :key) [{:keys [email] :as req} ]
   (notify-user-constrained email "Your key request in gitolite was approved" (<< "Please verify that your public key matches ~(:key req)") @config)
  )



(defn email-approved [approved]
  (doall (map deref (for [req approved] (future (email-request req))))))




