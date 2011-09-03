(ns gitolite-webui.persistency
    (:require 
     [clojure.contrib.datalog.database :as dlog]
     [gitolite-webui.db :as db])
    (:use [clojure.contrib.io :only (file)]
          [clojure.contrib.def :only (defonce-)]
          [clojure.set :only (difference)] 
          ))

(defonce- db (ref (dlog/make-database 
			  (relation :key-request [:name :email :key])
			  (index :key-request :name)
			  (relation :repo-request [:name :repo]) 
			  (index :repo-request :name))))

(defn- apply-type [rel t]
  (map #(with-meta % {:type t}) rel ))


(defn diff-watcher [action key ref old new]
  "apply action on difference found"
    (let [removed (apply difference (map #(get-in % [:repo-request :data]) [old new]))]
    	 (action removed)))

(defn initialize [db-file]
  "initializes persistency"
  (db/reload db-file db)
  (add-watch db nil (partial diff-watcher #(println %)))
  (db/periodical-save db-file db 5) 
  ) 

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
  (dosync (alter db (fn [db] (dlog/remove-tuple db (type req) (dissoc req :req-type))))))


