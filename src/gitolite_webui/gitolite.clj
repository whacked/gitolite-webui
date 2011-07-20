(ns gitolite-webui.gitolite
  (:use [clojure.contrib.io :only [file]] 
  	  [clojure.contrib.string :only [split-lines]]
  	  ))

(defn repo-name [repo-str]
     (last (re-matches #"repo\s+(.*)" repo-str)))

(defn- raw-repos [path]
	(filter #(.startsWith (first %) "repo")
		  (->> path slurp split-lines (partition-by empty?))))

(defn parse-conf [path]
	(reduce (fn [m repo] (assoc m (-> repo first repo-name keyword) (rest  repo))  ) {} (raw-repos path)))


