(ns gitolite-webui.test.gitolite
  (:require [gitolite-webui.gitolite :as g])
  (:use clojure.test midje.sweet gitolite-webui.config)
 )

(fact (g/repo-name "repo   play-0") => (just "play-0"))


(fact (g/parse-conf) => 
  (contains 
    {:play-lasting {"alice" "RW+" "bob" "RW+"}}
    {:moving-around {"ronenn" "RW+" "jenkins" "RW+"}}
    {:play-0 {"ronen" "RW+" "foo" "RW+" "bar" "RW+" "bob" "RW+" "alice" "RW+" "jenkins" "R"}}
    {:testing {"@all" "RW+"} }
    {:gitolite-admin {"ronenn" "RW+"}} 
    )	
  )

(fact (g/user-repo-manipulation {:name "bob" :repo "play-0"}) => (contains "repo    play-0\n        RW+     =   bob\n") )

(fact (g/pub-keys) => (contains ["alice.pub" "bob.pub"] :gaps-ok :in-any-order))
