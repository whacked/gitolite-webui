(ns gitolite-webui.core 
    (:use compojure.core gitolite-webui.view [gitolite-webui.config :only [admins]])
    (:require 
      (ring.middleware [multipart-params :as mp ] [params :as p] [session :as session])
      (ring.adapter [jetty :as jet])
      (ring.util [response :as res])
      (clojure.contrib [duck-streams :as ds])
      [gitolite-webui.persistency :as persist]
	[compojure.route :as route]
	[compojure.handler :as handler]))

(def *webdir* (str (ds/pwd) "/src/public"))

(persist/initialize "gitolite-db")

(defn process-ssh-upload [{:keys [name email file]}]
   (persist/persist-key-request name email (slurp (file :tempfile))))



(defroutes main-routes
           (GET  "/" [] (render index "gitolite webui"))
	     (GET "/upload-form.html" [] (render forms-layout upload-form "upload ssh key"))
	     (GET "/access-form.html" [] (render forms-layout (access-form-inc-repos) "request repository access")) 
	     (GET "/login-form.html" [] (render forms-layout login-form "Login to admin")) 
	     (GET "/admin-requests" {session :session}
	     	    (if (session :user) 
	     	    	 (render admin-layout admin-form "approve requests")
	     	       (res/redirect "/"))) 
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
             	 (render request-submited "request submited")
                )) 
	     (route/resources "/")
	     (route/not-found "Page not found"))

(def app (-> (handler/site main-routes) ))

(defn -main []
   (def server (jet/run-jetty #'app {:port 8080 :join? false})))

(defn stop [] (. server stop))

