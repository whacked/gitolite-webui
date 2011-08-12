(ns gitolite-webui.req-processor
    (:use 
      [clojure.contrib.json :as json]
    	[gitolite-webui.gitolite :only (add-key add-user-to-repo)]))

(defmulti process :type)
(defmethod process "ssh" [req] (add-key req))
(defmethod process "access" [req] (add-user-to-repo req))

(defn process-requests [requests]
   (doseq [json-req requests :let [req (json/read-json json-req true)]] 
   	  (process req)))
