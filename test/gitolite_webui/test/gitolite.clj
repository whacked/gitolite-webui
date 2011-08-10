(ns gitolite-webui.test.gitolite
  (:require [gitolite-webui.gitolite :as g] [fs :as fs])
  (:use 
    clojure.test
    midje.sweet
    gitolite-webui.config
    [clojure.java.io :only (file copy delete-file)]  
    ))

(def repo-home (@config :gitolite-home file))

(defn set-repo[]
  (fs/copy-tree (file "test/resources/") repo-home))

(defn cleanup [] 
   (fs/deltree repo-home))

(fact (g/repo-name "repo   play-0") => (just "play-0"))

(against-background [(before :facts (set-repo)) (after :facts (cleanup))]
   (fact (g/parse-conf) => 
	(contains 
	   {:play-lasting {"alice" "RW+" "bob" "RW+"}}
	   {:moving-around {"ronenn" "RW+" "jenkins" "RW+"}}
	   {:play-0 {"ronen" "RW+" "foo" "RW+" "bar" "RW+" "bob" "RW+" "alice" "RW+" "jenkins" "R"}}
	   {:testing {"@all" "RW+"} }
	   {:gitolite-admin {"ronenn" "RW+"}} 
	))		  
   (fact (g/user-repo-manipulation {:name "bob" :repo "play-0"}) => 
	(contains "repo    play-0\n        RW+     =   bob\n"))
   (fact (g/pub-keys) => (contains ["alice.pub" "bob.pub"] :gaps-ok :in-any-order))
   (fact (g/windows-format-key? (slurp "test/resources/keydir/alice.pub")) => false)  
   (fact (g/windows-format-key? (slurp "test/resources/id_rsa_broken.pub")) => false)  
   (fact (g/windows-format-key? (slurp "test/resources/id_rsa.pub")) => true)  
   )

(fact (g/format ...unix-key...) => ...unix-key...
   (provided 
     (g/windows-format-key? ...unix-key...) => false
     ))


