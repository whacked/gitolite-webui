(ns gitolite-webui.view
  (:use 
    [net.cgrand.enlive-html :only [deftemplate]])
  (:require 
    [gitolite-webui.gitolite :as git]
    [net.cgrand.enlive-html :as en]))

(defn render [t] (apply str t))

(def index (en/html-resource  "public/index.html"))

(deftemplate upload-success "public/upload-success.html" [])

(def upload-form (en/html-resource "public/upload-form.html")) 

(def access-form (en/html-resource "public/access-form.html")) 

(deftemplate forms-layout "public/forms-layout.html" [body title]
		[:body] (en/content body) 
		[:title] (en/content title))

(deftemplate general-layout "public/general-layout.html" [body title]
		[:body] (en/content body) 
		[:title] (en/content title))

(defn access-form-inc-repos []
  (en/transform access-form [:option] 
      (en/clone-for [repo (git/repos)]
		  		  (en/do-> 
		  		    (en/content repo)
		  		    (en/set-attr :value repo)))))
 
