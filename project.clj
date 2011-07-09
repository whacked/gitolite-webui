(defproject gitolite-webui "1.0"
  :description "A simple gitolite front end"
  :dependencies [[org.clojure/clojure "1.2.1"]
  		     [org.clojure/clojure-contrib "1.2.0"]
                 [ring "0.3.10"]
                 [compojure "0.6.4"]
                 [enlive "1.0.0"]
                 [hiccup "0.3.6"]
                 [com.sleepycat/je "4.0.92"]
                 [joda-time "1.6.2"] 
                 [midje "1.1.1"]
                 [cupboard "1.0.0-SNAPSHOT"]]
  
     :dev-dependencies [[lein-ring "0.4.5"]]
     :ring {:handler gitolite-webui.core/app}
  )
