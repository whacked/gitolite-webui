(ns gitolite-webui.notification
    (:use 
      gitolite-webui.trammel-checks
      gitolite-webui.config
      clojure.contrib.strint
      [match.core :only (match)]
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

(defn email-request [req]
  (match [req]
    [{:name _ :repo (r :when (comp not nil?)) :email e}] 
       (notify-user-constrained e "Your repository access request has been approved" (<< "Verify that you can access it by cloning ~(:repo req) repository.") @config)
    [{:name _ :key _ :email e}] 
       (notify-user-constrained e "Your key request in gitolite was approved" (<< "Please verify that your public key matches ~(:key req)") @config)
    ))

(defn email-approved [approved]
  (doseq [req approved] 
    (email-request req)))
 



