(ns gitolite-webui.routes
    (:use compojure.core gitolite-webui.view [gitolite-webui.config :only [admins]])
    (:require  
      (ring.middleware [multipart-params :as mp] [params :as p] [session :as session]) 
      (ring.util [response :as res]) 
      [gitolite-webui.persistency :as persist]
	[compojure.route :as route]
      [gitolite-webui.req-processor :as process]))

(defn process-ssh-upload [{:keys [name email file]}]
   (persist/persist-key-request name email (slurp (file :tempfile))))
 
(defroutes main-routes
           (GET  "/" [] (render index "gitolite webui"))
	     (GET "/upload-form" [] (render forms-layout upload-form "upload ssh key"))
	     (GET "/access-form" [] (render forms-layout (access-form-inc-repos) "request repository access")) 
	     (GET "/login-form" [] (render forms-layout login-form "Login to admin")) 
	     (GET "/admin-requests" {session :session}
	     	    (if (session :user) 
	     	    	 (render admin-layout (admin-form-with-data) "approve requests")
	     	       (res/redirect "/login-form"))) 
	     (mp/wrap-multipart-params 
              (POST "/ssh-upload" {params :params} 
                (process-ssh-upload params)
                (render ssh-upload "ssh upload done"))) 
           (POST "/access-request" [name repo]
                (persist/persist-repo-request name repo)
                (render request-submited "request submited"))
           (POST "/login" [name pass session] 
           	     (if (and (-> pass nil? not) (.equals pass (admins name))) 
           	     	   (assoc (res/redirect "/admin-requests") :session (assoc session :user name) )  
           	     	   "Failed to login"
           	     	 )) 
           (p/wrap-params 
             (POST "/process-requests" {params :params form-params :form-params} 
                (let [requests (form-params "requests")]
             	(if (string? requests)
             	   (process/process-requests (vector requests))  
             	   (process/process-requests requests)  
             	   )) 
             	 "done"
                )) 
	     (route/resources "/")
	     (route/not-found "Page not found"))


