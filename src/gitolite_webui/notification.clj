(ns gitolite-webui.notification
    (:use 
      gitolite-webui.trammel-checks
      gitolite-webui.config
      clojure.contrib.strint
      clojure.contrib.logging
      [clojure.core.match.core :only (match)]
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

(defn email-request [req]
  (match [req]
	   [{:name _ :repo (r :when (comp not nil?)) :email email}] (notify-user-constrained email "Your repository access request has been approved" (<< "Verify that you can access it by cloning ~(:repo req) repository.") @config)
	   [{:name _ :key _ :email email}] (notify-user-constrained email "Your key request in gitolite was approved" (<< "Please verify that your public key matches ~(:key req)") @config)
	   ))

(defn email-approved [approved]
  (doall (pmap deref (for [req approved] (future (email-request req))))))




