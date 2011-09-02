(ns gitolite-webui.view
  (:use 
    [gitolite-webui.persistency :only [ssh-pending access-pending]]
    [net.cgrand.enlive-html :only [deftemplate defsnippet transform set-attr at attr=]]
    [clojure.template :only [do-template]] 
     clojure.contrib.strint) 
  (:require 
    (clojure.contrib [error-kit :as kit]) 
    [clojure.contrib.json :as json]
    [gitolite-webui.gitolite :as git]
    [net.cgrand.enlive-html :as en]))

(do-template [name] 
  (deftemplate name (str "public/" 'name ".html") [title body]
	[:body] (en/content body) 
	[:title] (en/content title))
      forms-layout general-layout admin-layout)

(do-template [name meta] 
    (def name (with-meta (en/html-resource (str "public/"  'name ".html")) meta))
      index {:title "Gitolite webui" :layout general-layout}
      upload-form {:title "Upload ssh key" :layout forms-layout}
      access-form {:title "Request repository access" :layout forms-layout}
      admin-form {:title "Approve requests" :layout admin-layout}
      login-form {:title "Login to admin" :layout forms-layout})

(defn access-form-inc-repos []
  (with-meta (en/transform access-form [:option] 
      (en/clone-for [repo (git/repos)]
		  		  (en/do-> 
		  		    (en/content repo)
		  		    (en/set-attr :value repo)))) (meta access-form)))

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

(defn re-apply-params [form params]
   (reduce 
     (fn [f [k v]] 
   	  (at f [(keyword (str "input#" (name k)))] (set-attr :value v))) form params))

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
  (with-meta (en/transform admin-form [:option] 
      (en/clone-for [req (requests) :let [[s val] req]]
		  		  (en/do-> 
		  		    (en/content s)
		  		    (en/set-attr :value val)))) (meta admin-form)))

(defsnippet form-success "public/form-success.html" [:#wrapper] [title desc]
	    [:h1] (en/content title)
	    [:#description] (en/content desc))

(def ssh-upload 
  (with-meta 
   (form-success "Key uploaded successfully" 
   	(list "You can now proceed to requesting access to "
   	  {:tag :a :attrs {:href "/access-form"} :content "repositories."}))
       {:title "Upload done" :layout general-layout}))

(def request-submited 
   (with-meta  
     (form-success "Access request submited" "An email will be sent to you once its approved.") 
        {:title "request submited" :layout general-layout}))

(kit/deferror *missing-meta*[] [m]
    {:msg m
     :unhandled (kit/throw-msg RuntimeException)}) 

(defn render [t] 
   (if-not (every? identity ((juxt :title :layout) (meta t)))
     (kit/raise *missing-meta* "Missing :layout and :title meta on form")
     (->> t ((-> t meta :layout) (-> t meta :title)) (apply str))))

