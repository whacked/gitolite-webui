(ns gitolite-webui.config
    (:require [clojure.contrib.json :as json])
    (:use [clojure.contrib.io :only (file)]
     clojure.contrib.def 
     ))

(defn read-config [env] 
  (let [resolution {:dev "test/resources/" :prod "./"}]	
    (json/read-json (slurp (file (resolution env) "gitolite-webui.js")))))

(defonce config (atom (read-config :dev)))

(defn prod [] (swap! config (fn [_] (read-config :prod))))

(defn admins [user] 
  (get-in @config [:admins (keyword user)]))
