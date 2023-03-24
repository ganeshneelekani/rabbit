(ns rabbit.server
  (:require
   [com.stuartsierra.component :as component]
   [rabbit.components.api-server :as api-server]
   [rabbit.config :as c]
   [clojure.tools.logging :as log]
   [clojure.java.io :as io]))

(defn create-system
  [config]
  (component/system-map
   :config config
   :rabbit-mq-config (api-server/rabbit-mq-service (:rabbit-mq-config config))
   :api-server (component/using
                (api-server/service (:service-map config))
                [:rabbit-mq-config])))

(defn configure-logger
  "Logback requires extra steps to be pointed to a different location programatically"
  []
  (if (.exists (io/as-file "resources/logback.xml"))
    (let [log-factory (org.slf4j.LoggerFactory/getILoggerFactory)
          configurator (ch.qos.logback.classic.joran.JoranConfigurator.)]
      (.setContext configurator log-factory)
      (.reset log-factory)
      (.doConfigure configurator "resources/logback.xml"))
    (log/warn "No logback.xml found. Defaulting to System.out")))

(defn -main
  [& args]
  (let [config c/server-config]
    (configure-logger)
    (component/start (create-system config))))
