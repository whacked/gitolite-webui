(ns gitolite-webui.test.db
    (:require
	[gitolite-webui.db :as db] 
	[clojure.contrib.datalog.database :as dlog])
    (:use midje.sweet 
    	    [clojure.java.io :only (file delete-file)]
    	    ))

(defonce tuples [[:alice {:bob 1}] [:alice {:bob 2}] [:bob {:alice 2}]])  

(defonce test-db (ref (dlog/make-database 
				(relation :alice [:bob])
				(relation :bob [:alice])
				(index :alice :bob)
				(index :bob :alice))))

(defn apply-tuples [db f tuples]
	(if-let [tuple (first tuples)] 
         (apply-tuples (f db (first tuple) (second tuple)) f (rest tuples))
          db))

(def *db-file* "/tmp/test-db")

(defn- save-and-sleep []
  (db/periodical-save *db-file* test-db 1)
  (. Thread sleep 1500)   
  (. @db/timer purge) 
  )

(defn cleanup []
   (delete-file (file *db-file*))
   (dlog/empty-database test-db))

(defn save-reload-cycle [save-fn] 
	(dosync (alter test-db (fn [db] (apply-tuples db dlog/add-tuple tuples)))) 
	(save-fn)
	(dosync (alter test-db (fn [db] (apply-tuples db dlog/remove-tuple tuples)))) 
      (db/reload *db-file* test-db))

(against-background [(before :contents (save-reload-cycle #(db/save *db-file* test-db)) :after (cleanup))] 
  (fact (:data (dlog/get-relation test-db :alice)) => (just [{:bob 1} {:bob 2}]))
  (fact (:data (dlog/get-relation test-db :bob)) => (just [{:alice 2}])))

(against-background [(before :contents (save-reload-cycle #(save-and-sleep)) :after (cleanup))]
  (fact (:data (dlog/get-relation test-db :alice)) => (just [{:bob 1} {:bob 2}]))
  (fact (:data (dlog/get-relation test-db :bob)) => (just [{:alice 2}])))


