(ns gitolite-webui.config
    (:import (java.io File))
    (:require [clojure.contrib.json :as json])
    (:use [clojure.contrib.io :only (file)]
     clojure.contrib.def 
     ))

(defonce resolution {:dev "test/resources/" :prod "./"})

(defn read-config [env] 
    (json/read-json (slurp (file (resolution env) "gitolite-webui.js"))))

(defonce config 
    (if (.exists (File. (resolution :dev))) 
      (atom (read-config :dev))))

(defn prod [] (swap! config (fn [_] (read-config :prod))))

(defn admins [user] 
  (get-in @config [:admins (keyword user)]))

(defn connection-settings [] 
   {:pre [(@config :db)] :post [(not-any? nil? (map #(get-in @config [:db %]) [:user :password :subname] ))]}
   (merge (@config :db) {:classname "org.h2.Driver" :subprotocol "h2:file" }))

