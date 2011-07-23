(ns gitolite-webui.gitolite
    (:use [clojure.contrib.io :only [file]] 
	    [clojure.contrib.string :only [split-lines split as-str]]
	    [clojure.contrib.macros :only [letfn- ]]
          [clojure.contrib.io :only (file)]
           gitolite-webui.config 
          ))

(defn repo-name [repo-str]
	(last (re-matches #"repo\s+(.*)" repo-str)))

(defn- raw-repos [path]
	 (filter #(.startsWith (first %) "repo")
		   (->> path slurp split-lines (partition-by empty?))))

(defn permissions [lines]
	(let [pair (fn [line] (->> line (split #"\=") (map #(.trim %)) reverse (into [])))] 
		  (into {} (reduce (fn [m line] (conj m (pair line))) [] lines))))

(defn parse-conf [path]
	(reduce (fn [m repo] 
			(assoc m (-> repo first repo-name keyword) (permissions (rest repo))))
		  {} (raw-repos path)))

(defn resolve []
	(str (:gitolite-home *config*) "gitolite.conf"))

(defn repos []
	(map as-str (keys (-> "conf/gitolite.conf" resolve parse-conf))))

(defn pub-keys [] 
	(map #(.getName %) (file-seq (-> "test/resources/keydir" resolve file ))))
