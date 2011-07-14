(ns gitolite-webui.test.db
    (:require
	[gitolite-webui.db :as db] 
	[clojure.contrib.datalog.database :as dlog])
    (:use midje.sweet))

(defonce tuples [[:alice {:bob 1}] [:alice {:bob 2}]]) 
(defonce test-db (ref (dlog/make-database 
				(relation :alice [:bob])
				(relation :bob [:alice])
				(index :alice :bob)
				(index :bob :alice))))

(defn apply-tuples [db f tuples]
	(if-let [tuple (first tuples)] 
         (apply-tuples (f db (first tuple) (second tuple)) f (rest tuples))
          db
	  ))


(defn save-sequence [] 
	(dosync 
	  (alter test-db 
	  	   (fn [db] (apply-tuples db dlog/add-tuple tuples )))) 
	(db/save "/tmp/test-db" test-db)
	(dosync 
	  (alter test-db (fn [db] (apply-tuples db dlog/remove-tuple tuples)))) 
      (db/reload "/tmp/test-db" test-db))

(fact  (:data (dlog/get-relation test-db :alice)) => (just [{:bob 1} {:bob 2}])
	 (against-background (before :facts (save-sequence))))


