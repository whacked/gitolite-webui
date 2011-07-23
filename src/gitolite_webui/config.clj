(ns gitolite-webui.config
  (:require [clojure.contrib.json :as json])
  (:use [clojure.contrib.io :only (file)]))

(def *config* (json/read-json (slurp (file "gitolite-webui.js"))))
