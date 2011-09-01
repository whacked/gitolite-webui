(ns gitolite-webui.routes
    (:use compojure.core gitolite-webui.view [gitolite-webui.config :only [admins]])
    (:require  
      (ring.middleware [multipart-params :as mp] [params :as p] [session :as session]) 
      (ring.util [response :as res]) 
      [gitolite-webui.persistency :as persist]
      [gitolite-webui.validations :as valid]
	[compojure.route :as route]
      [gitolite-webui.req-processor :as process]))

(defn process-ssh-upload [{:keys [name email file]}]
   (persist/persist-key-request name email (slurp (file :tempfile))))
 


(defroutes main-routes
           (GET  "/" [] (render index))

	     (GET "/upload-form" [] 
	     	    (render upload-form))

	     (GET "/access-form" [] (render (access-form-inc-repos))) 

	     (GET "/login-form" [] (render login-form )) 

	     (GET "/admin-requests" {session :session}
	     	    (if (session :user) 
	     	    	 (render (admin-form-with-data))
	     	       (res/redirect "/login-form"))) 

	     (mp/wrap-multipart-params 
              (POST "/ssh-upload" {params :params} 
                (if-let [errors (valid/upload-validate params)]
                   (render (-> upload-form (with-errors errors) (re-apply-params params))) 
                   (do (process-ssh-upload params) 
                     (render ssh-upload)))))

           (POST "/access-request" [name repo]
                (persist/persist-repo-request name repo)
                (render request-submited))

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


