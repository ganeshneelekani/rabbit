(ns rabbit.server
  (:require
   [com.stuartsierra.component :as component]
   [rabbit.components.api-server :as api-server]
   [rabbit.config :as c]))

(defn create-system
  [config]
  (component/system-map
   :config config
   :rabbit-mq-config (api-server/rabbit-mq-service (:rabbit-mq-config config))
   :api-server (component/using
                (api-server/service (:service-map config))
                [:rabbit-mq-config])))

(defn -main
  []
  (let [config c/server-config]
    (component/start (create-system config))))
