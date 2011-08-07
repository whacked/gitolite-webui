(ns gitolite-webui.gitolite
    (:use [clojure.contrib.io :only [file]] 
	    [clojure.contrib.string :only [split-lines split as-str]]
	    [clojure.contrib.macros :only [letfn- ]]
          [clojure.string :only (replace-first)]
          [clojure.core :only [re-find]]
           gitolite-webui.config 
          ))

(defn repo-name [repo-str]
	(last (re-matches #"repo\s+(.*)" repo-str)))

(defn- resolve-path [path]
	(str (:gitolite-home @config) path))

(defn- gitoconf []
	(resolve-path "conf/gitolite.conf"))

(defn- slurp-conf []
   (slurp (gitoconf)))

(defn- raw-repos []
	 (filter #(.startsWith (first %) "repo")
		   (->> (slurp-conf) split-lines (partition-by empty?))))

(defn permissions [lines]
	(let [pair (fn [line] (->> line (split #"\=") (map #(.trim %)) reverse (into [])))] 
		  (into {} (reduce (fn [m line] (conj m (pair line))) [] lines))))

(defn parse-conf []
	(reduce (fn [m repo] 
			(assoc m (-> repo first repo-name keyword) (permissions (rest repo))))
		  {} (raw-repos)))

(defn repos []
	(map as-str (keys (parse-conf))))

(defn pub-keys [] 
	(map #(.getName %) (file-seq (-> "keydir/" resolve-path file ))))


(defn- perm-to-user [user perm] (str "        " perm "     =   "   user))

(defn user-repo-manipulation [{:keys [name repo]}]
  (let [conf (slurp-conf) pattern (->> ["repo\\s+" repo] (apply str) re-pattern) match (re-find pattern conf)]
    (replace-first conf pattern (str match "\n" (perm-to-user name "RW+")))))

(defn add-user-to-repo  [req]
     (spit gitoconf (user-repo-manipulation req)))

