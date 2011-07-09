(ns gitolite-webui.core 
    (:use compojure.core )
    (:require 
      [net.cgrand.enlive-html :as html]
	[compojure.route :as route]
	[compojure.handler :as handler]))


(defroutes main-routes
	     (route/files "/index.html")
	     (route/files "/upload-form.html")
	     (route/files "/forms.css")
	     (route/resources "/")
	     (route/not-found "Page not found"))

(def app
     (handler/site main-routes))

