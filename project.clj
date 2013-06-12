(defproject gitolite-webui "1.0"
  :description "A simple gitolite front end"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 ;; [org.clojure/clojure-contrib "1.2.0"]
                 [org.clojure/data.json "0.2.2"]
                 [org.clojure/core.incubator "0.1.2"]
                 [org.clojure/tools.logging "0.2.6"]
                 [ring/ring-devel "1.2.0-beta1"]
                 [ring/ring-jetty-adapter "1.1.8"]
                 [compojure "1.1.5"]
                 [enlive "1.1.1"]
                 [hiccup "1.0.3"]
                 [midje "1.6-alpha2"]
                 [clj-decline "0.0.5"]
                 [pretzel "0.2.4"]
                 [org.apache.commons/commons-email "1.3.1"]
                 [trammel "0.7.0"]
                 [fs "1.3.3"]
                 [log4j "1.2.17"] 
                 [korma "0.3.0-RC5"]
                 [com.h2database/h2 "1.3.172"]
                 [org.clojure/tools.cli "0.2.2"]]

     :main gitolite-webui.core
     :dev-dependencies [[lein-ring "0.8.5"]
                        [lein-midje "3.0.1"]
                        [fs "1.3.3"]]
     :ring {:handler gitolite-webui.core/app}
  )
