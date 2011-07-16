(ns gitolite-webui.core 
    (:use 
    	 compojure.core
    	[net.cgrand.enlive-html :only [deftemplate defsnippet content clone-for nth-of-type first-child do-> set-attr sniptest at emit* content html-resource]] )
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

(def upload-form (html-resource "public/upload-form.html")) 

(def upload-form (html-resource "public/access-form.html")) 

(deftemplate forms-layout "public/forms-layout.html" [body title]
		[:body] (content body) 
		[:title] (content title))


(defn process-ssh-upload [{:keys [name email file]}]
  (persist/persist-key-request name email (slurp (file :tempfile))))

(defroutes main-routes
           (GET  "/" [] (render (index)))

	     (GET "/upload-form.html" [] (render (forms-layout upload-form "upload ssh key")))
	     (GET "/access-form.html" [] (render (forms-layout upload-form "request repository access")))

	     (mp/wrap-multipart-params 
              (POST "/ssh-upload" {params :params} 
                (process-ssh-upload params)
                (render (upload-success)))) 

	     (route/resources "/")

	     (route/not-found "Page not found"))

(def app (handler/site main-routes))

