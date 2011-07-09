(ns gitolite-webui.core 
    (:use 
    	 compojure.core
    	[net.cgrand.enlive-html :only [deftemplate defsnippet content clone-for nth-of-type first-child do-> set-attr sniptest at emit*]] )
    (:require 
      (ring.middleware [multipart-params :as mp])
      (clojure.contrib [duck-streams :as ds])
      [gitolite-webui.persistency :as persist]
      [net.cgrand.enlive-html :as html]
	[compojure.route :as route]
	[compojure.handler :as handler]))

(def *webdir* (str (ds/pwd) "/src/public"))
(defn render [t] (apply str t))
(persist/initialize "gitolite-db")

(deftemplate index "public/index.html" [])
(deftemplate upload-success "public/upload-success.html" [])

(defn upload-file [file]
  (ds/copy (file :tempfile) (ds/file-str "file.out"))
  (render (upload-success)))


(defroutes main-routes
           (GET  "/" [] (render (index)))
	     (route/files "/upload-form.html")
	     (mp/wrap-multipart-params 
            (POST "/ssh-upload" {params :params} (upload-file (:file params)))) 
	     (route/resources "/")
	     (route/not-found "Page not found"))

(def app (handler/site main-routes))

