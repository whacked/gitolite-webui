(ns gitolite-webui.persistency
    (:use 
     [clojure.string :only (lower-case replace)]
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

(defn- existing-tables [db]
  (with-connection h2-connection
    (into #{}
      (map #(-> % :table_name lower-case keyword)
        (resultset-seq (-> (connection) (.getMetaData) (.getTables nil nil "%" nil)))))))

(defn- ^{:test (fn [] (= (lowercase-keys {:BLA 1}) {:bla 1}))}
  lowercase-keys [upper-keys-map] 
   (reduce (fn [m [k v]] (assoc m (-> k name lower-case keyword) v)) {} upper-keys-map))

(defn- apply-type [result type] (with-meta result {:type type}))

(def entities
  (letfn [(lower-with-type [type val] (-> val lowercase-keys (apply-type type)))]
    {:contact {:key :name :fields [:name :email] :t-fn lowercase-keys}
     :key-request {:key :name :fields [:name :key] :t-fn (partial lower-with-type :key-request)}
     :repo-request {:key [:name :repo] :fields [:name :repo] :t-fn (partial lower-with-type :repo-request)}}))

(doseq [[entity-key {:keys [key fields t-fn]}] entities :let [entity-name (name entity-key) table-key (-> entity-name str (replace #"\-" "_") keyword)]] 
  (intern 'gitolite-webui.persistency (symbol entity-name) ; dynamic defentity
    (-> 
      (create-entity entity-name) 
      (pk key) 
      (table table-key) 
      (entity-fields fields) 
      (transform t-fn))))

(defn schema-statement [statment name fields]
  (concat (list statment name) fields))

(defmacro create-and-drop [connection tables]
  `(do  
    (defn create-schema []
     (with-connection ~connection
     ~@(for [[name fields] tables] (schema-statement 'create-table name fields) ))) 
    (defn drop-schema []
     (with-connection ~connection
     ~@(for [[name & fields] tables] (schema-statement 'drop-table name '()))))))

(create-and-drop h2-connection
    {:contact [[:name "varchar"] [:email "varchar"]] 
     :repo_request [["PRIMARY KEY" "(name, repo)"] [:name "varchar"] [:repo "varchar"]] 
     :key_request [[:name "varchar"] [:key "varchar"]]}) 

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


