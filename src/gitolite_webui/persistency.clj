(ns gitolite-webui.persistency
    (:require [cupboard.core :as cup]))

(defn initialize [db-folder]
     (cup/open-cupboard! db-folder)
     (cup/defpersist public-key-request ((:user :index :unique) (:email) (:ssh-key :index :unique) (:pending)))) 

(defn ssh-pending []
     (cup/query (:pending = "true")))

(defn persist-key-request [user email ssh]
	(cup/make-instance public-key-request [user email ssh "true"])
	)
