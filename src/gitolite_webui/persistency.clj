(ns gitolite-webui.persistency
    (:require 
	[clojure.contrib.datalog.database :as dlog]
      [gitolite-webui.db :as db])
    (:use [clojure.contrib.io :only (file)] [clojure.contrib.def :only (defonce-)] ))

(defonce- db (ref (dlog/make-database 
			     (relation :key-request [:name :email :key])
			     (index :key-request :name)
			     (relation :repo-request [:name :repo]) 
			     (index :repo-request :name))))

(defn- apply-type [rel t]
  (map #(with-meta % {:type t}) rel ))

(defn initialize [db-file]
	(db/reload db-file db)
      (db/periodical-save db-file db 5)) 

(defn ssh-pending [] 
	(-> @db :key-request :data (apply-type :key-request)))

(defn- add-request [db relation request]
	 (dlog/add-tuple db relation request))

(defn persist-key-request [name email key]
	(dosync (alter db add-request :key-request {:name name :email email :key key })))

(defn access-pending []
   (-> @db :repo-request :data (apply-type :repo-request)))

(defn persist-repo-request [name repo]
	(dosync (alter db add-request :repo-request {:name name :repo repo })))

(defn clear-request [req]
	(dosync (alter db (fn [db] (dlog/remove-tuple (type req) req)))))

