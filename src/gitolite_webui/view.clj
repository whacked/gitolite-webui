(ns gitolite-webui.view
  (:use 
    [gitolite-webui.persistency :only [ssh-pending access-pending]]
    [net.cgrand.enlive-html :only [deftemplate defsnippet]])
  (:require 
    [clojure.contrib.json :as json]
    [gitolite-webui.gitolite :as git]
    [net.cgrand.enlive-html :as en]))

(def index (en/html-resource "public/index.html"))

(def upload-form (en/html-resource "public/upload-form.html")) 

(def access-form (en/html-resource "public/access-form.html")) 

(def admin-form (en/html-resource "public/admin-form.html")) 

(def login-form (en/html-resource "public/login-form.html")) 

(deftemplate forms-layout "public/forms-layout.html" [title body]
		[:body] (en/content body) 
		[:title] (en/content title))

(deftemplate general-layout "public/general-layout.html" [title body]
		[:body] (en/content body) 
		[:title] (en/content title))

(deftemplate admin-layout "public/admin-layout.html" [title body]
		[:body] (en/content body) 
		[:title] (en/content title))

(defn render
	([t title] (->> t (general-layout title) (apply str)))
	([layout t title] (->> t (layout title) (apply str))))

(defn access-form-inc-repos []
  (en/transform access-form [:option] 
      (en/clone-for [repo (git/repos)]
		  		  (en/do-> 
		  		    (en/content repo)
		  		    (en/set-attr :value repo)))))

(defn request-as-json [req]
   (json/json-str (assoc req :type (type req))))

(defmulti request-option type)
(defmethod request-option :ssh [req]
  (conj [] (str (:name req) ":key-request") (request-as-json req)))
(defmethod request-option :access [req]
 (conj [] (str (:name req) "-" (:repo req) ":repo-request") (request-as-json req)))

(defn- requests []
   (map #(request-option %) (concat (ssh-pending) (access-pending))))

(defn admin-form-with-data  []
  (en/transform admin-form [:option] 
      (en/clone-for [req (requests) :let [[s val] req]]
		  		  (en/do-> 
		  		    (en/content s)
		  		    (en/set-attr :value val)))))

(defsnippet form-success "public/form-success.html" [:#wrapper] [title desc]
	    [:h1] (en/content title)
	    [:#description] (en/content desc))

(def ssh-upload 
     (form-success "Key uploaded successfully" 
   		(list "You can now proceed to requesting access to " {:tag :a :attrs {:href "/access-form"} :content "repositories."})))


(def request-submited (form-success "Access request submited" "An email will be sent to you once its approved."))
