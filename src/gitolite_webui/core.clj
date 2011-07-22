(ns gitolite-webui.core 
    (:use 
    	 compojure.core
    	[net.cgrand.enlive-html :only [deftemplate]])
    (:require 
      (ring.middleware [multipart-params :as mp])
      (clojure.contrib [duck-streams :as ds])
      [gitolite-webui.persistency :as persist]
      [gitolite-webui.gitolite :as git]
      [net.cgrand.enlive-html :as en]
	[compojure.route :as route]
	[compojure.handler :as handler]))

(def *webdir* (str (ds/pwd) "/src/public"))

(defn render [t] (apply str t))

(persist/initialize "gitolite-db")

(def index (en/html-resource  "public/index.html"))

(deftemplate upload-success "public/upload-success.html" [])

(def upload-form (en/html-resource "public/upload-form.html")) 

(def access-form (en/html-resource "public/access-form.html")) 

(deftemplate forms-layout "public/forms-layout.html" [body title]
		[:body] (en/content body) 
		[:title] (en/content title))

(deftemplate general-layout "public/general-layout.html" [body title]
		[:body] (en/content body) 
		[:title] (en/content title))

(defn access-form-inc-repos []
  (en/transform access-form [:option] 
      (en/clone-for [repo (git/repos)]
		  		  (en/do-> 
		  		    (en/content repo)
		  		    (en/set-attr :value repo)))))

(defn process-ssh-upload [{:keys [name email file]}]
   (persist/persist-key-request name email (slurp (file :tempfile))))

(defroutes main-routes
           (GET  "/" [] (render (general-layout index "gitolite webui")))

	     (GET "/upload-form.html" [] (render (forms-layout upload-form "upload ssh key")))

	     (GET "/access-form.html" [] (render (forms-layout (access-form-inc-repos) "request repository access"))) 
	     (mp/wrap-multipart-params 
              (POST "/ssh-upload" {params :params} 
                (process-ssh-upload params)
                (render (upload-success)))) 

	     (route/resources "/")

	     (route/not-found "Page not found"))

(def app (handler/site main-routes))

