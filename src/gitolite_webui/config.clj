(ns gitolite-webui.config
    (:import (java.io File))
    (:require [clojure.data.json :as json])
    (:use [gitolite-webui.util :only (file)]
     ))

(defonce resolution {:dev "test/resources/" :prod "./"})

(defn read-config [env] 
    (json/read-str (slurp (file (resolution env) "gitolite-webui.js"))
                   :key-fn keyword))

(defonce config (atom nil))

(defn dev [] (swap! config (fn [_] (read-config :dev))))
(defn prod [] (swap! config (fn [_] (read-config :prod))))

(defn admins [user] 
  (get-in @config [:admins (keyword user)]))

(defn connection-settings [] 
   {:pre [(@config :db)] :post [(not-any? nil? (map #(get-in @config [:db %]) [:user :password :subname] ))]}
   (merge (@config :db) {:classname "org.h2.Driver" :subprotocol "h2:file" }))

