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

(defn simple-logging-middleware [app]
  (fn [req]
    (println req)
    (app req)))

(defroutes main-routes
           (GET  "/" [] (render index "gitolite webui"))
	     (GET "/upload-form.html" [] (render forms-layout upload-form "upload ssh key"))
	     (GET "/access-form.html" [] (render forms-layout (access-form-inc-repos) "request repository access")) 
	     (GET "/admin-requests" []  (render admin-layout admin-form "approve requests")) 
	     (mp/wrap-multipart-params 
              (POST "/ssh-upload" {params :params} 
                (process-ssh-upload params)
                (render ssh-upload "ssh upload done"))) 
           (POST "/access-request" [name repo]
                (persist/persist-repo-request name repo)
                (render request-submited "request submited"))
           (mp/wrap-multipart-params 
           (POST "/process-requests" {params :params}
                (println params) 
                (render request-submited "request submited")
                )) 
	     (route/resources "/")
	     (route/not-found "Page not found"))

(def app (handler/site main-routes))

