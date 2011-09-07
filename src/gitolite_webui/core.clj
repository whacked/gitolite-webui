(ns gitolite-webui.core 
    (:use compojure.core)
    (:require 
      (ring.adapter [jetty :as jet])
      (clojure.contrib [duck-streams :as ds])
      [gitolite-webui.persistency :as persist]
      [gitolite-webui.routes :as routes]
      [gitolite-webui.config :as conf]
	[compojure.handler :as handler]))

(def *webdir* (str (ds/pwd) "/src/public"))

(def app (-> (handler/site routes/main-routes)))

(defn -main []
   (conf/prod) 
   (persist/initialize "gitolite-db")
   (def server (jet/run-jetty #'app  {:port 8081 :join? false})))

(defn stop [] (. server stop))

(defn restart []
 (stop) 
 (-main)
  )
