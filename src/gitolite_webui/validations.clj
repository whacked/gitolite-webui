(ns gitolite-webui.validations
    (:use decline.core)
    (:require [pretzel.strings :as str]))

(defn required [param]
  (validate-val param seq {param ["this is a required field"]}))

(defn exists? [param]
  (validate-val param 
		    (fn [param] (-> param :tempfile (. exists))) {param ["A file must be selected!"]}))

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
	 (required :name)))


(def login-validate
     (validations
	 (required :user) 
	 (required :pass)
	 )    
     )
