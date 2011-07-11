(defproject gitolite-webui "1.0"
  :description "A simple gitolite front end"
  :dependencies [[org.clojure/clojure "1.2.1"]
  		     [org.clojure/clojure-contrib "1.2.0"]
                 [ring "0.3.10"]
                 [compojure "0.6.4"]
                 [enlive "1.0.0"]
                 [hiccup "0.3.6"]
                 [midje "1.1.1"]]
  
     :dev-dependencies [[lein-ring "0.4.5"]]
     :ring {:handler gitolite-webui.core/app}
  )
