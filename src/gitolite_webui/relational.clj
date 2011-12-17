(ns gitolite-webui.relational
   (:use korma.db korma.core 
         clojure.contrib.sql
         gitolite-webui.config))

(defn connection-settings [] 
   {:pre  [(@config :db)] :post [(not-any? nil? (map #(get-in @config [:db %]) [:user :password :subname] ))]}
   (merge (@config :db) {:classname "org.h2.Driver" :subprotocol "h2:file" }))

(def h2-connection (connection-settings))

(defdb gitolite-db h2-connection)

(defentity contact
  (pk :name)
  (table :contact) 
  (entity-fields :name :email))

(defentity key-request
  (pk :id)
  (table :key_req) 
  (entity-fields :name :ssh_key))

(defentity acc-request
  (pk :id)
  (table :acc_req) 
  (entity-fields :name :repo))

(defn create-schema []
  (with-connection h2-connection
    (create-table :contact [:name "varchar"] [:email "varchar"])
    (create-table :acc_req [:id "identity"] [:name "varchar"] [:repo "varchar"])
    (create-table :key_req [:id "identity"] [:name "varchar"] [:ssh_key "varchar"])))

(defn drop-schema []
  (with-connection h2-connection
    (drop-table :acc_req)
    (drop-table :contact)
    (drop-table :key_req)))

