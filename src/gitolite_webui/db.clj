(ns gitolite-webui.db
    (:import (java.util Timer TimerTask))
    (:use [clojure.contrib.io :only (file)])
    (:require 
	[clojure.contrib.json :as json]
	[clojure.contrib.datalog.database :as dlog]))

(defn save [db-file db]
	(spit (file db-file) (json/json-str @db)))

(defn periodical-save [db-file db interval]
	(let [timer (Timer.)]
	  (. timer schedule  (proxy [TimerTask] [] (run [] (save db-file db)))  (* interval 1000))))

(defn- into-tuple [hash relation]
	 (into [] (flatten (cons relation (seq hash)))))

(defn- tuples-of [relation]
	 (into []  (map #(vector (first relation) %) (-> relation (second) (:data)))))

(defn- db-map-tuples [db-map]
	 (first (filter (comp not empty?) 
			    (reduce (fn [acc relation ] (conj acc (tuples-of relation))) [] (into [] db-map)))))

(defn reload [db-file db]
	(if (-> db-file (file) (. exists))
	  (let [db-map (json/read-json (slurp (file db-file))) ]
	    (doseq [tuple (db-map-tuples db-map)] 
		     (dosync (alter db 
			 	  (fn [db] (dlog/add-tuple db (first tuple) (second tuple)))))))))
