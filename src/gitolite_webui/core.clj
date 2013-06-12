(ns gitolite-webui.core 
    (:gen-class)
    (:use compojure.core clojure.tools.cli)
    (:require 
      (ring.adapter [jetty :as jet])
      [gitolite-webui.persistency :as persist]
      [gitolite-webui.routes :as routes]
      [gitolite-webui.config :as conf]
      [gitolite-webui.notification :as notify]
	[compojure.handler :as handler]))

(def *webdir* (str (System/getProperty "user.dir") "/src/public"))

(def app (-> (handler/site routes/main-routes)))


(defmacro with-help [& [args _ action _  & specs]]
  `(let [[options# ign# banner#] (cli (next ~args)  ~@specs ["-h" "--help" "Show help" :default false :flag true])] 
    (if (options# :help)
     (println banner#) 
     (~action options#))))

(defn start-app [args]
  (with-help args
    :action (fn [options]
             (conf/prod) 
             (persist/initialize-db) 
             (def server (jet/run-jetty #'app  {:port (options :port) :join? false})))
     :specs  ["-p" "--port" "The port to listen on" :default 8081 :parse-fn #(Integer. %)]
    ))

(defn validate-email [args]
 (with-help args 
    :action (fn [options]
              (conf/prod) 
               (notify/notify-user (options :email) "test email from gitolite webui" 
                        "If your reading this then your email configuration is working")) 

    :specs ["-e" "--email" "Email Address to test with"]
    ))

(defn -main [& args]
  (condp = (first args)
    "start" (start-app args)
    "validate-email" (validate-email args)))

(defn stop [] (. server stop))

(defn restart []
  (stop) 
  (-main))
