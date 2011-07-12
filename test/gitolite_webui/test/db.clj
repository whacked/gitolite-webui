(ns gitolite-webui.test.db
    (:require
	[gitolite-webui.db :as db] 
	[clojure.contrib.datalog.database :as dlog])
    (:use clojure.test midje.sweet))

(defonce test-db (ref (dlog/make-database 
				(relation :alice [:bob])
				(relation :bob [:alice])
				(index :alice :bob)
				(index :bob :alice)
				)))

(defn save-sequence [] 
	(dosync 
	  (alter test-db (fn [db] (dlog/add-tuple db :alice {:bob 1})))) 
	(db/save "/tmp/test-db" test-db)
	)

(fact  => (db/reload "/tmp/test-db" test-db) (just [{:bob 1}])
	 (against-background (before :facts (save-sequence))))


