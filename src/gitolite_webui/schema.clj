(ns gitolite-webui.schema
   (:use korma.db korma.core
      [clojure.java.jdbc :only (with-connection connection create-table drop-table)]
    clojure.tools.logging
    [gitolite-webui.config :only (connection-settings)]))

(defn- apply-type [result type] (with-meta result {:type type}))

(defn- ^{:test (fn [] (= (lowercase-keys {:BLA 1}) {:bla 1}))}
  lowercase-keys [upper-keys-map] 
   (reduce (fn [m [k v]] (assoc m (-> k name clojure.string/lower-case keyword) v)) {} upper-keys-map))

(def transforms
  (letfn [(lower-with-type [type val] (-> val lowercase-keys (apply-type type)))]
    {:contact lowercase-keys
     :key-request (partial lower-with-type :key-request)
     :repo-request (partial lower-with-type :repo-request)}))

(def entities {:contact {:key :name :fields [:name :email]}
               :key-request {:key :name :fields [:name :key]}
               :repo-request {:key [:name :repo] :fields [:name :repo]}})

(doseq [[entity-key {:keys [key fields]}] entities 
  :let [entity-name (name entity-key)
        table-key (-> entity-name
                    str
                    (clojure.string/replace #"\-" "_")
                    keyword)]]
  (intern 'gitolite-webui.persistency (symbol entity-name) ; dynamic defentity
    (-> 
      (create-entity entity-name) 
      (pk key) 
      (table table-key) 
      (entity-fields fields) 
      (transform (transforms entity-key)))))

(defn- existing-tables []
  (with-connection (connection-settings)
    (into #{}
      (map #(-> % :table_name clojure.string/lower-case keyword)
        (resultset-seq (-> (connection) (.getMetaData) (.getTables nil nil "%" nil)))))))

(defn- act-on-table [condition name todo]
  `(~condition ((existing-tables) ~name) ~todo))

(defn- schema-statement 
  ([statment name] (schema-statement statment name '()))
  ([statment name fields] 
   `(do 
     (trace (str ~statment " " ~name " " ~@fields))
     (~statment ~name ~@fields))))

(defmacro create-and-drop [tables-map]
  `(do  
    (defn create-schema []
      (with-connection (connection-settings)
          ~@(for [[name fields] tables-map] 
              (act-on-table 'if-not name (schema-statement 'create-table name fields))))) 
    (defn drop-schema []
      (with-connection (connection-settings)
        ~@(for [[name fields] tables-map] 
            (act-on-table 'if name (schema-statement 'drop-table name)))))))

(create-and-drop 
    {:contact [[:name "varchar"] [:email "varchar"]] 
     :repo_request [["PRIMARY KEY" "(name, repo)"] [:name "varchar"] [:repo "varchar"]] 
     :key_request [[:name "varchar"] [:key "varchar"]]})

#_(macroexpand  '(create-and-drop 
    {:contact [[:name "varchar"] [:email "varchar"]] 
     :repo_request [["PRIMARY KEY" "(name, repo)"] [:name "varchar"] [:repo "varchar"]] 
     :key_request [[:name "varchar"] [:key "varchar"]]})) 
 
