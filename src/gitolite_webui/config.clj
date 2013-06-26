(ns gitolite-webui.config
    (:import (java.io File))
    (:require
     [clj-yaml.core :as yaml]
     )
    (:use [gitolite-webui.util :only (file)]
     ))

(defonce resolution {:dev "test/resources/" :prod "./"})

;; TODO
;; i'd actually strip the slash and handle path joins later
(defn read-config [env]
  (let [conf (yaml/parse-string (slurp (file (resolution env) "config.yml")))
        gitolite-home-path (conf :gitolite-home)]
    (if-not (.endsWith gitolite-home-path "/")
      (assoc conf :gitolite-home (str gitolite-home-path "/"))
      conf)))

(defonce config (atom nil))

(defn dev [] (swap! config (fn [_] (read-config :dev))))
(defn prod [] (swap! config (fn [_] (read-config :prod))))

(defn admins [user] 
  (get-in @config [:admins (keyword user)]))

(defn connection-settings [] 
   {:pre [(@config :db)] :post [(not-any? nil? (map #(get-in @config [:db %]) [:user :password :subname] ))]}
   (merge (@config :db) {:classname "org.h2.Driver" :subprotocol "h2:file" }))

