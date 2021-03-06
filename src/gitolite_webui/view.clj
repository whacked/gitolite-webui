(ns gitolite-webui.view
    (:use 
     [clojure.string :only [capitalize]]
     [gitolite-webui.persistency :only [ssh-pending access-pending]]
     [net.cgrand.enlive-html :only [deftemplate defsnippet  defsnippets transform set-attr at attr= snippet]]
     [clojure.template :only [do-template]] 
     clojure.tools.logging
     clojure.core.strint
       [slingshot.slingshot :only [throw+ try+]]
       
       ) 
    (:require 
     [clojure.data.json :as json]
     [gitolite-webui.gitolite :as git]
     [net.cgrand.enlive-html :as en]))


(deftemplate general-layout "public/general-layout.html" [{:keys  [title main]}]
				  [:div.main] (en/substitute (main)) 
				  [:title] (en/content title))

(en/html-resource (str "public/"  "index.html"))

(do-template [name title forms] 
		 (def name {:main (snippet (str "public/"  'name ".html") [:div.main] []) :title title }) index "Gitolite webui" []
		 access-form "Request repository access" [[:option (en/clone-for [repo (git/repos)]
												     (en/do-> 
													 (en/content repo)
													 (en/set-attr :value repo)))]]
		 upload-form "Upload ssh key"  []
		 login-form "Login to admin" [])

(def access-form {:main (snippet "public/access-form.html" [:div.main] [] 
				  [:option] (en/clone-for [repo (git/repos)]
					      (en/do-> 
					   	  (en/content repo)
					         (en/set-attr :value repo)))) :title "Request repository access"})

(defn 
  ^{:test (fn [] (with-errors upload-form [[:email ["this is a required field"]]]))} 
  with-errors [form errors]
  (if-let [pair (first errors)]
	    (with-errors 
		(at form 
		    [(keyword (str "div#" (-> pair first name)))] (set-attr :class "clearfix error")
		    [(attr= :for (-> pair first name))] (en/content (-> pair second flatten)))
		(rest errors)) 
	    form 
	    ))

(defn re-apply-params [form params]
  (reduce 
    (fn [f [k v]] 
	  (at f [(keyword (str "input#" (name k)))] (set-attr :value v))) form params))

(defn request-as-json [req]
  (json/write-str (assoc req :req-type (type req))))

(defmulti request-option type)
(defmethod request-option :key-request [req]
  (conj [] (str (:name req) ":key-request") (request-as-json req)))
(defmethod request-option :repo-request [req]
  (conj [] (str (:name req) "-" (:repo req) ":repo-request") (request-as-json req)))

(defn- requests []
	 (map #(request-option %) (concat (ssh-pending) (access-pending))))

(def admin-form {:main (snippet "public/admin-form.html" [:div.main] []
				 [:option] (en/clone-for [req (requests) :let [[s val] req]]
						     (en/do-> 
							 (en/content s)
							 (en/set-attr :value val)))) :title "Approve requests"})

(defsnippets "public/index.html" 
		 [index-mid-up [:div.hero-unit] []]
		 [index-content [:div.content] []])

(do-template [name title desc] 
	(defsnippet name "public/form-success.html" [:#wrapper] []
		[:h1] (en/content title)
		[:#description] (en/content desc))	 
	      ssh-upload-snip "Key uploaded successfully"  (list "You can now proceed to requesting access to " {:tag :a :attrs {:href "/access-form"} :content "repositories."})
	      request-submited-snip  "Access request submited" "An email will be sent to you once its approved."
	      requests-processed-snip  "Requests processed" "All selected requests were commited and marked as processed." )

(def ssh-upload {:title "Upload done" :main ssh-upload-snip})

(def request-submited {:title "request submited" :main request-submited-snip})

(def requests-processed { :title "requests processed"  :main requests-processed-snip})

;; XXX unchecked usage of throw+
(defn render [t] 
  (if-not (t :title)
	    (throw+ {
              :type ::missing-meta
              :message "Missing :title meta on form"
              :unhandled RuntimeException
              })
	    (->> t general-layout  (apply str))))

