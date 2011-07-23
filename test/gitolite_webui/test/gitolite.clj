(ns gitolite-webui.test.gitolite
  (:require [gitolite-webui.gitolite :as g])
  (:use clojure.test midje.sweet)
 )

(fact (g/repo-name "repo   play-0") => (just "play-0"))


(fact (g/parse-conf "test/resources/conf/gitolite.conf") => 
  (contains 
    {:play-lasting {"alice" "RW+" "bob" "RW+"}}
    {:moving-around {"ronenn" "RW+" "jenkins" "RW+"}}
    {:play-0 {"ronen" "RW+" "foo" "RW+" "bar" "RW+" "bob" "RW+" "alice" "RW+" "jenkins" "R"}}
    {:testing {"@all" "RW+"} }
    {:gitolite-admin {"ronenn" "RW+"}} 
    )	
  )

(fact (g/pub-keys) => (contains ["alice.pub" "bob.pub"] :gaps-ok :in-any-order))
