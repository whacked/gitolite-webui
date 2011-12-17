(defproject gitolite-webui "1.0"
  :description "A simple gitolite front end"
  :dependencies [[org.clojure/clojure "1.2.1"]
  		     [org.clojure/clojure-contrib "1.2.0"]
                 [ring "0.3.10"]
                 [compojure "0.6.5"]
                 [enlive "1.0.0"]
                 [hiccup "0.3.6"]
                 [midje "1.3.0"]
                 [clj-decline "0.0.5"]
                 [pretzel "0.2.2"]
                 [org.apache.commons/commons-email "1.2"]
                 [trammel "0.6.0-SNAPSHOT"]
                 [fs "0.8.1"]
                 [log4j "1.2.16"] 
                 [korma "0.2.1"]
                 [com.h2database/h2 "1.3.161"]
                 ]

     :main gitolite-webui.core
     :dev-dependencies [[lein-ring "0.4.5"][lein-midje "1.0.7"] [fs "0.8.1"]]
     :ring {:handler gitolite-webui.core/app}
  )
