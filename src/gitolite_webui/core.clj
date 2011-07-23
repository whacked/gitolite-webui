(ns gitolite-webui.core 
    (:use 
    	 compojure.core
    	 gitolite-webui.view)
    (:require 
      (ring.middleware [multipart-params :as mp])
      (clojure.contrib [duck-streams :as ds])
      [gitolite-webui.persistency :as persist]
	[compojure.route :as route]
	[compojure.handler :as handler]))

(def *webdir* (str (ds/pwd) "/src/public"))

(persist/initialize "gitolite-db")

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

