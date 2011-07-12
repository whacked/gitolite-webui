(ns gitolite-webui.persistency
    (:require 
	[clojure.contrib.datalog.database :as dlog]
      [gitolite-webui.db :as db])
    (:use [clojure.contrib.io :only (file)] [clojure.contrib.def :only (defonce-)] ))

(defonce- db (ref (dlog/make-database 
			     (relation :request [:name :email :key])
			     (index :request :name))))

(defn initialize [db-file]
	(db/reload db-file db)) 

(defn ssh-pending [] 
	(dlog/select @db :request {:name "ronen"}))

(defn- add-request [db request]
	 (dlog/add-tuple db :request request))

(defn persist-key-request [name email key ]
	(dosync 
	  (alter db add-request {:name name :email email :key key })))
