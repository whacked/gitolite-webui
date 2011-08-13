(ns gitolite-webui.core 
    (:use compojure.core gitolite-webui.view [gitolite-webui.config :only [admins]])
    (:require 
      (ring.middleware [multipart-params :as mp ] [params :as p] [session :as session] [reload :as reload])
      (ring.adapter [jetty :as jet])
      (ring.util [response :as res])
      (clojure.contrib [duck-streams :as ds])
      [gitolite-webui.persistency :as persist]
      [gitolite-webui.req-processor :as process]
      [gitolite-webui.config :as conf]
	[compojure.route :as route]
	[compojure.handler :as handler]))

(def *webdir* (str (ds/pwd) "/src/public"))


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
             	 (process/process-requests (form-params "requests"))
             	 "done"
                )) 
	     (route/resources "/")
	     (route/not-found "Page not found"))

(def app (-> (handler/site main-routes)))

(defn -main []
   (conf/prod) 
   (persist/initialize "gitolite-db")
   (def server (jet/run-jetty #'app  {:port 8081 :join? false})))

(defn stop [] (. server stop))

