(ns gitolite-webui.persistency
    (:require 
     [clojure.contrib.datalog.database :as dblog]
     [gitolite-webui.db :as db])
    (:use 
     [gitolite-webui.notification :only (email-approved)]
     gitolite-webui.debug
     [clojure.contrib.io :only (file)]
     [clojure.contrib.datalog.rules :only (<- ?- rules-set)]
     [clojure.contrib.def :only (defonce-)]
     [clojure.set :only (difference)] 
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

(defn- user-email [req]
  (assoc req :email (-> (dblog/select @db :contact {:name (req :name)}) first :email)))

(defn diff-watcher [action enrich ref key old-db new-db]
  "Apply action on enriched approved requests (difference found between old and new)."
  (let [[old-req new-req] (map #((juxt (comp :data :repo-request) (comp :data :key-request)) %) [old-db new-db])]
    (doseq [[o n] (map list old-req new-req) :let [approved (difference o n)] :when (not-empty approved)] 
      (action (map enrich approved)))))


(defn initialize [db-file]
  "Initializes persistency"
  (db/reload db-file db)
  (add-watch db nil (partial diff-watcher email-approved user-email))
  (db/periodical-save db-file db 5)) 

(defn ssh-pending [] 
  (-> @db :key-request :data (apply-type :key-request)))

(defn- add-request [db relation request]
	 "Adds a request to the db, in case that a request with same name exists it will be replaced"
	 (if-let [existing (first (dblog/select db relation {:name (request :name)}))]
        (dblog/add-tuple (dblog/remove-tuple db relation existing) relation request)
        (dblog/add-tuple db relation request)))

(defn persist-key-request [name email key]
  (dosync 
    (alter db add-request :key-request {:name name :key key })
    (alter db add-request :contact {:name name :email email })
    ))

(defn access-pending []
  (-> @db :repo-request :data (apply-type :repo-request)))

(defn persist-repo-request [name repo email]
  (dosync 
    (alter db add-request :repo-request {:name name :repo repo })
    (alter db add-request :contact {:name name :email email })))

(defn clear-request [req]
  (dosync (alter db (fn [db] (dblog/remove-tuple db (type req) (dissoc req :req-type))))))


