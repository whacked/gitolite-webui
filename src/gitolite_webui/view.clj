(ns gitolite-webui.view
  (:use 
    [net.cgrand.enlive-html :only [deftemplate defsnippet]])
  (:require 
    [gitolite-webui.gitolite :as git]
    [net.cgrand.enlive-html :as en]))

(def index (en/html-resource "public/index.html"))

(def upload-form (en/html-resource "public/upload-form.html")) 

(def access-form (en/html-resource "public/access-form.html")) 

(deftemplate forms-layout "public/forms-layout.html" [title body]
		[:body] (en/content body) 
		[:title] (en/content title))

(deftemplate general-layout "public/general-layout.html" [title body]
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

(defsnippet form-success "public/form-success.html" [:#wrapper] [title desc]
	    [:h1] (en/content title)
	    [:#description] (en/content desc))

(def ssh-upload 
     (form-success "Key uploaded successfully" 
   		(list "You can now proceed to requesting access to " {:tag :a :attrs {:href "/access-form.html"} :content "repositories."})))


(def request-submited (form-success "Access request submited" "An email will be sent to you once its approved."))
