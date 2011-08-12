(ns gitolite-webui.req-processor
    (:use 
      [gitolite-webui.persistency :only (clear-request)]
      [clojure.contrib.json :as json]
    	[gitolite-webui.gitolite :only (add-key add-user-to-repo)]))

(defmulti process type)
(defmethod process :key-request [req] (add-key req))
(defmethod process :repo-request [req] (add-user-to-repo req))

(defn process-requests [requests]
   (doseq [json-req requests :let [req (json/read-json json-req true) typed-req (with-meta req {:type (-> req :req-type keyword)})]] 
   	  (process typed-req)
   	  (clear-request typed-req) 
   	  ))
