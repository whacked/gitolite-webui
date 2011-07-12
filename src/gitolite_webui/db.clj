(ns gitolite-webui.db
    (:import (java.util Timer TimerTask))
    (:use [clojure.contrib.io :only (file)])
     (:require [clojure.contrib.json :as json]))


(defn save [db-file db]
	 (spit (file db-file) (json/json-str @db)))

(defn schduled-save [db-file db]
	(let [timer (Timer.)]
	  (. timer schedule  (proxy [TimerTask] [] (run [] (save db-file db)))  (* 5 1000))))

(defn reload [db-file db]
	(if (-> db-file (file) (. exists))
	  (let [data (json/read-json (slurp (file db-file)))]
	    (get-in data [:bob :data ]) 
	    )))
