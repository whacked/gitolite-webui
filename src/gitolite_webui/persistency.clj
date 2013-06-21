(ns gitolite-webui.persistency
    (:use 
     [gitolite-webui.notification :only (email-request)]
      gitolite-webui.debug gitolite-webui.schema
     [gitolite-webui.util :only (file)]
     [datalog.rules :only (<- ?- rules-set)]
     [clojure.set :only (difference)] 
      clojure.tools.logging
      korma.db korma.core 
      gitolite-webui.config))

;; prevents $tablename from becoming "$tablename" in h2's sql
;; https://groups.google.com/forum/#!topic/sqlkorma/KS_kGVjPs6I
(korma.config/set-delimiters "")

(defn initialize-db [] 
  (defdb gitolite-db (connection-settings))
  ;Create schema will create a table only if it does not exist alreay
  (create-schema))

(defn reset-schema [] 
  (drop-schema) 
  (create-schema))

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

(defn user-email [req]
  (-> (select contact (where {:name (req :name)})) first :email))

(defn- add-email [req] 
       (assoc req :email (user-email req)))
 
(defn clear-request [req]
  (let [entity (key-to-entity (type req)) ]
    (delete entity (where (dissoc req :req-type)))
    (future (email-request (add-email req)))))


