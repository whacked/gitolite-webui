(ns gitolite-webui.view
  (:use 
    [gitolite-webui.persistency :only [ssh-pending access-pending]]
    [net.cgrand.enlive-html :only [deftemplate defsnippet transform set-attr at attr=]]
    [clojure.template :only [do-template]] 
     clojure.contrib.strint) 
  (:require 
    [clojure.contrib.json :as json]
    [gitolite-webui.gitolite :as git]
    [net.cgrand.enlive-html :as en]))

(do-template [name] 
    (def name (en/html-resource (str "public/"  'name ".html")))
      index upload-form access-form admin-form login-form) 

(do-template [name] 
  (deftemplate name (str "public/" 'name ".html") [title body]
	[:body] (en/content body) 
	[:title] (en/content title))
      forms-layout general-layout admin-layout)


(defn render
	([t title] (->> t (general-layout title) (apply str)))
	([layout t title] (->> t (layout title) (apply str))))

(defn access-form-inc-repos []
  (en/transform access-form [:option] 
      (en/clone-for [repo (git/repos)]
		  		  (en/do-> 
		  		    (en/content repo)
		  		    (en/set-attr :value repo)))))

(defn 
  ^{:test (fn [] (with-errors upload-form [[:email ["this is a required field"]]]))} 
  with-errors [form errors]
    (if-let [pair (first errors)]
       (with-errors 
         (at form 
         	 [(keyword (str "input#" (-> pair first name)))] (set-attr :class "error")
         	 [(attr= :for (-> pair first name))] (en/content (-> pair second flatten)))
         (rest errors)) 
       form 
      ))

(defn re-apply-params [form params])


(defn request-as-json [req]
   (json/json-str (assoc req :req-type (type req))))

(defmulti request-option type)
(defmethod request-option :key-request [req]
  (conj [] (str (:name req) ":key-request") (request-as-json req)))
(defmethod request-option :repo-request [req]
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
