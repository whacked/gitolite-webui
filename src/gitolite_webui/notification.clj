(ns gitolite-webui.notification
    (:use 
      gitolite-webui.trammel-checks
      gitolite-webui.config
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

(defn email-approved [approved]
  (doseq [req approved :let [email (:email req)]] 
	   (notify-user-constrained email "Your request has been approved" "congrated approved" @config)))
 



