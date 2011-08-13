(ns gitolite-webui.validations
  (:use decline.core)
  (:require [pretzel.strings :as str]))

(defn required [param]
  (validate-val param seq {param ["this is a required field"]}))

(defn exists? [param]
    (validate-val param 
    	(fn [param] (-> param :filename nil?)) {param ["A file must be selected!"]}))

(def upload-validate
 (validations
   (required :name)
   (validate-some
    (required :email)
    (validate-val :email str/looks-like-email?
                  {:email ["must contain an @ sign, and a dot in the domain name"]}))
   
   (exists? :file)))
