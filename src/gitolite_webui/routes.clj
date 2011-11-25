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

(defn validate [form params validation succ]
  (if-let [errors (validation params)]
	    (render (update-in form [:main] #(fn [] ( -> (%) (with-errors errors) (re-apply-params params ))))) 
	    (succ)))

(defroutes main-routes
	     (GET  "/" [] (render index))

	     (GET "/upload-form" [] 
		    (render upload-form))

	     (GET "/access-form" [] (render access-form))

	     (GET "/login-form" [] (render login-form )) 

	     (GET "/admin-requests" {session :session}
		    (if (session :user) 
			(render admin-form)
			(res/redirect "/login-form"))) 

	     (mp/wrap-multipart-params 
		 (POST "/ssh-upload" {params :params} 
			 (validate upload-form params valid/upload-validate 
				     (fn [] (process-ssh-upload params) 
					   (render ssh-upload)))))

	     (POST "/access-request" [name email repo :as {params :params}] 
		     (validate access-form params valid/access-validate
			 (fn [] 
			     (persist/persist-repo-request name repo email) 
			     (render request-submited))))

	     (POST "/login" [user pass session :as {params :params}] 
		     (validate login-form params valid/login-validate
			  (fn [] (if (.equals pass (admins user)) 
				     (assoc (res/redirect "/admin-requests") :session (assoc session :user user) )  
					"Failed to login")))) 

	     (p/wrap-params 
		 (POST "/process-requests" {params :params form-params :form-params} 
			 (let [requests (form-params "requests")]
			   (if (string? requests)
			     (process/process-requests (vector requests))  
			     (process/process-requests requests)  
			     )) 
                    (render requests-processed))) 

	     (route/resources "/")
	     (route/not-found "Page not found"))


