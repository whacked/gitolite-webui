(ns gitolite-webui.validations
    (:use 
       decline.core
       [gitolite-webui.persistency :only [user-email]] 
       clojure.contrib.logging)
       
    (:require 
       [pretzel.strings :as str]))

(defn required [param]
  (validate-val param seq {param ["this is a required field"]}))


(defn user-has-email [{:keys [name email] :as params}]
   (some not-empty [email (user-email params)]))

(defn exists? [param]
  (validate-val param 
    (fn [param] (-> param :size (> 0))) {param ["A file must be selected!"]}))

(def upload-validate
     (validations
	 (required :name)
	 (exists? :file)
	 (validate-some
	   (required :email)
	   (validate-val :email str/looks-like-email?
			     {:email ["must contain an @ sign, and a dot in the domain name"]}))))

(def access-validate
     (validations 
     	 (required :name)
     	 (validation user-has-email {:email ["No email was found in system"]})))


(def login-validate
     (validations
	 (required :user) 
	 (required :pass)))
