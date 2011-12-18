(ns gitolite-webui.persistency
    (:use 
     [clojure.string :only (lower-case)]
     [gitolite-webui.notification :only (email-approved)]
      gitolite-webui.debug
     [clojure.contrib.io :only (file)]
     [clojure.contrib.datalog.rules :only (<- ?- rules-set)]
     [clojure.contrib.def :only (defonce-)]
     [clojure.set :only (difference)] 
      korma.db korma.core 
      clojure.contrib.sql
      gitolite-webui.config))

(defn connection-settings [] 
   {:pre [(@config :db)] :post [(not-any? nil? (map #(get-in @config [:db %]) [:user :password :subname] ))]}
   (merge (@config :db) {:classname "org.h2.Driver" :subprotocol "h2:file" }))

(defn initialize-db []
  (def h2-connection (connection-settings))
  (defdb gitolite-db h2-connection))

(defn- ^{:test (fn [] (= (lowercase-keys {:BLA 1}) {:bla 1}))}
  lowercase-keys [upper-keys-map] 
   (reduce (fn [m [k v]] (assoc m (-> k name lower-case keyword) v)) {} upper-keys-map))

(defn- apply-type [result type] (with-meta result {:type type}))

(defentity contact
  (pk :name)
  (table :contact) 
  (entity-fields :name :email)
  (transform lowercase-keys))

(defentity key-request
  (pk :name)
  (table :key_req) 
  (entity-fields :name :key)
  (transform #(-> % lowercase-keys (apply-type :key-request))))

(defentity repo-request
  (pk [:name :repo])
  (table :acc_req) 
  (entity-fields :name :repo)
  (transform #(-> % lowercase-keys (apply-type :repo-request))))

(defn create-schema []
  (with-connection h2-connection
    (create-table :contact [:name "varchar"] [:email "varchar"])
    (create-table :acc_req ["PRIMARY KEY" "(name, repo)"] [:name "varchar"] [:repo "varchar"] )
    (create-table :key_req [:name "varchar"] [:key "varchar"])))

(defn drop-schema []
  (with-connection h2-connection
    (drop-table :acc_req)
    (drop-table :contact)
    (drop-table :key_req)))
 
(defn connection-settings [] 
   {:pre  [(@config :db)] :post [(not-any? nil? (map #(get-in @config [:db %]) [:user :password :subname] ))]}
   (merge (@config :db) {:classname "org.h2.Driver" :subprotocol "h2:file" }))

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
    (delete entity (where req))))


