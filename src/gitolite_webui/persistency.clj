(ns gitolite-webui.persistency
    (:require 
	[clojure.contrib.json :as json]
	[clojure.contrib.datalog.database :as db])

    (:use [clojure.contrib.io :only (file)] [clojure.contrib.def :only (defonce-)] ))

(defonce- db (ref (db/make-database 
			     (relation :request [:name :email :key])
			     (index :request :name))))

(defn- save-db [db-file]
	 (spit (file db-file) (json/json-str @db)))

(defn- reload-db [db-file]
	 (json/read-json (slurp (file db-file))))



(defn initialize [db-file]) 

(defn ssh-pending [] 
   (db/select @db :request {:name "ronen"}))

(defn- add-request [db request]
	 (db/add-tuple db :request request))

(defn persist-key-request [name email key ]
	(dosync 
	  (alter db add-request {:name name :email email :key key })))
