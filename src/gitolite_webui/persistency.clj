(ns gitolite-webui.persistency
    (:require 
     [clojure.contrib.datalog.database :as dblog]
     [gitolite-webui.db :as db])
    (:import 
     (org.apache.commons.mail SimpleEmail DefaultAuthenticator))
    (:use 
     gitolite-webui.config
     gitolite-webui.trammel-checks
     gitolite-webui.debug
     [clojure.contrib.io :only (file)]
     [clojure.contrib.datalog.rules :only (<- ?- rules-set)]
     [clojure.contrib.def :only (defonce-)]
     [clojure.set :only (difference)] 
     [trammel.core :only (with-constraints contract)] 
     ))


(defonce- db (ref (dblog/make-database 
			  (relation :contact [:name :email])
			  (index :contact :name)
			  (relation :key-request [:name :key])
			  (index :key-request :name)
			  (relation :repo-request [:name :repo]) 
			  (index :repo-request :name))))

(defn- apply-type [rel t]
	 (map #(with-meta % {:type t}) rel ))

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
		   "defines constraints for notify-user"
		   [to subject body config] 
		   [(non-nil-params notify-user) 
		    (keys-not-empty (:email config) :user :pass :host)
		    ]))

(def notify-user-constrained (with-constraints notify-user notify-user-contract))

(defn user-email [req]
 (->  (dblog/select @db :contact {:name (req :name)}) first :email))

(defn notify-approved [approved]
  (doseq [a approved :let [email (user-email a)]] 
	   (notify-user-constrained email "Your request has been approved" "congrated approved" @config)))

(defn diff-watcher [action key ref old-db new-db]
  "apply action on approved requests (difference found between old and new)."
  (let [[old-req new-req] (map #((juxt (comp :data :repo-request) (comp :data :key-request)) %) [old-db new-db])]
    (doseq [[o n] (map list old-req new-req) :let [approved (difference o n)] :when (not-empty approved)] 
      (action approved))))

(defn initialize [db-file]
  "initializes persistency"
  (db/reload db-file db)
  (add-watch db nil (partial diff-watcher notify-approved))
  (db/periodical-save db-file db 5)) 

(defn ssh-pending [] 
  (-> @db :key-request :data (apply-type :key-request)))

(defn- add-request [db relation request]
	 (dblog/add-tuple db relation request))

(defn persist-key-request [name email key]
  (dosync 
    (alter db add-request :key-request {:name name :key key })
    (alter db add-request :contact {:name name :email email })
    ))

(defn access-pending []
  (-> @db :repo-request :data (apply-type :repo-request)))

(defn persist-repo-request [name repo]
  (dosync (alter db add-request :repo-request {:name name :repo repo })))

(defn clear-request [req]
  (dosync (alter db (fn [db] (dblog/remove-tuple db (type req) (dissoc req :req-type))))))


