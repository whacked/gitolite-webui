(ns gitolite-webui.req-processor
  (:require
     [clojure.data.json :as json]
     )
    (:use 
     [gitolite-webui.persistency :only (clear-request)]
     [gitolite-webui.gitolite :only (add-key add-user-to-repo)]))

(defmulti process type)
(defmethod process :key-request [req] (add-key req))
(defmethod process :repo-request [req] (add-user-to-repo req))

(defn process-requests [requests]
  (doseq [json-req requests :let [req (json/read-str json-req :key-fn keyword) typed-req (with-meta req {:type (-> req :req-type keyword)})]] 
	   (process typed-req)
	   (clear-request typed-req)))
