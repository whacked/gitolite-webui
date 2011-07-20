(ns gitolite-webui.test.gitolite
  (:require [gitolite-webui.gitolite :as g])
  (:use clojure.test midje.sweet)
 )

(fact (g/repo-name "repo   play-0") => (just "play-0"))

(fact (g/parse-conf "test/resources/gitolite.conf") => (just {}))

