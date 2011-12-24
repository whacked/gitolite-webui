(ns gitolite-webui.persistency
    (:use 
     [gitolite-webui.notification :only (email-approved)]
      gitolite-webui.debug gitolite-webui.schema
     [clojure.contrib.io :only (file)]
     [clojure.contrib.datalog.rules :only (<- ?- rules-set)]
     [clojure.contrib.def :only (defonce-)]
     [clojure.set :only (difference)] 
      korma.db korma.core 
      clojure.contrib.sql
      gitolite-webui.config))



(defn initialize-db [] (defdb gitolite-db (connection-settings)))

(defn reset-schema [] 
  (drop-schema) 
  (create-schema))

(defn user-email [req]
  (select contact (where {:name (req :name)})))

(defn- add-email [req] 
       (assoc req :email (user-email req)))

#_(defn diff-watcher [action enrich ref key old-db new-db]
  "Apply action on enriched approved requests (difference found between old and new)."
  (let [[old-req new-req] (map #((juxt (comp :data :repo-request) (comp :data :key-request)) %) [old-db new-db])]
    (doseq [[o n] (map list old-req new-req) :let [approved (difference o n)] :when (not-empty approved)] 
      (action (map enrich approved)))))

(defn ssh-pending [] (select key-request))

(defn- add-request [relation request]
	 "Adds a request to the db, in case that a request with same name exists it will be replaced"
	 (if-let [existing (first (select relation (where {:name (request :name)})))]
         (update relation  (set-fields request) (where {:name (request :name)})) 
         (insert relation (values request))))

(defn persist-key-request [name email key]
  (add-request key-request {:name name :key key })
  (add-request contact {:name name :email email }))

(defn access-pending [] (select repo-request ))

(defn persist-repo-request [name repo email]
  (insert repo-request (values {:name name :repo repo }))
  (add-request contact {:name name :email email }))

(defn- key-to-entity [type-key]
  (->> type-key name symbol (ns-resolve 'gitolite-webui.persistency) deref))

(defn clear-request [req]
  (let [entity (key-to-entity (type req)) ]
    (delete entity (where (dissoc req :req-type)))))


