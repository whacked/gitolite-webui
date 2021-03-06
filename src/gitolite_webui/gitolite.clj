(ns gitolite-webui.gitolite
    (:use
     [gitolite-webui.util :only (file)]
     [clojure.string :only (replace-first)]
     [clojure.core :only [re-find]]
     [clojure.java.shell :only [sh]]
     clojure.core.strint
     clojure.tools.logging
     gitolite-webui.config
     gitolite-webui.util
     ))

(defn git 
  ([action]  (git action [])) 
  ([action args] 
    (let [res (apply sh "git" (name action) (into args [:dir (@config :gitolite-home)]))]
       (if (not= (:exit res) 0)
          (error (:err res)) 
          (info (:out res)))
        res
       )))

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
		   (->> (slurp-conf) split-lines (remove #(.startsWith % "#")) (partition-by blank?))))

(defn permissions [lines]
  (let [pair (fn [line] (->> (clojure.string/split line #"\=") (map #(.trim %)) reverse (into [])))] 
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

(defn add-user-to-repo  [{:keys [name repo] :as req} ]
  (let [conf (gitoconf)]
    (spit conf (user-repo-manipulation req)) 
    (git :add ["conf"])  
    (git :commit ["-m" (<< "Adding ~{name} to ~{repo}")])))

(defn convert-windows [key host]
  (<< "ssh-rsa ~(apply str (drop-last (nthnext (.split key \"\\n\") 2))) ~{host}\n" ))

(def windows-key-mathces
     [#"---- BEGIN SSH2 PUBLIC KEY ----"
	#"Comment: \".*\"" 
	#".*"
	#".*"
	#".*"
	#".*"
	#"---- END SSH2 PUBLIC KEY ----"
	])

(defn windows-format-key? [key]
  (let [lines (.split key "\n")]
    (and (= (alength lines) (.length windows-key-mathces))
	   (every? not 
		     (map (fn [[match line]] (nil? (re-find match line))) 
			    (partition 2 (interleave windows-key-mathces lines)))))))

(defn formatk [key]
  (if (windows-format-key? key)
    (convert-windows key) 
    key
    ))

(defn add-key [{:keys [name key]}]
  (let [formated-key (formatk key) key-file (str "keydir/" name ".pub" )]
    (spit (resolve-path key-file) key)
    (git :add [key-file])
    (git :commit ["-m" (<< "Adding key for ~{name}")])))

(defn stage [files]
  "Stages given files in git" 
  (doseq [f files] (sh "git" "add" file)))



