(ns gitolite-webui.notification
    (:use 
      gitolite-webui.trammel-checks
      gitolite-webui.config
      clojure.contrib.strint
      [trammel.core :only (with-constraints contract)])
    (:import 
     (org.apache.commons.mail SimpleEmail DefaultAuthenticator)))


(defn- notify-user [to subject body config]
   (let [{:keys [user pass host port ssl]} (:email config)]
     (doto (SimpleEmail.)
	 (.setHostName host) 
	 (.setSmtpPort port)
	 (.setAuthenticator (DefaultAuthenticator. user  pass))
       (.setTLS ssl)
       (.setFrom "gookup@gmail.com")
       (.setSubject subject)
       (.setMsg body)
       (.addTo to)
       (.send))))

(def notify-user-contract 
     (contract notify-user-constraints 
		   "Defines constraints for notify-user"
		   [to subject body config] 
		   [(non-nil-params notify-user) 
		    (keys-not-empty (:email config) :user :pass :host)
		    ]))

(def notify-user-constrained (with-constraints notify-user notify-user-contract))

(defmulti email-request type)
(defmethod email-request :key-request [req]
   (notify-user-constrained (:email req) "Your key request in gitolite was approved" (<< "Please verify that your public key matches ~(:key req)") @config))
(defmethod email-request :repo-request [req]
  (notify-user-constrained (:email req) "Your repository access request has been approved" (<< "Verify that you can access it by cloning ~(:repo req) repository.") @config))

(defn email-approved [approved]
  (doseq [req approved] 
    (email-request req)))
 



