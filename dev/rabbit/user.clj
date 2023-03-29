(ns rabbit.user
  (:require [rabbit.config :as c]
            [rabbit.server :as server]
            [com.stuartsierra.component :as component]
            [rabbit.routes :as r]
            [io.pedestal.http :as http]
            [com.stuartsierra.component.repl :as cr]
            [io.pedestal.test :as pt]
            [clojure.pprint :as p]))


(defonce system-ref (atom nil))

(defn system [_]
  (-> c/server-config
      (server/create-system)))

(cr/set-init system)


(defn start-dev []
  (cr/start))

(defn stop-dev []
  (cr/stop))

(defn restart-dev []
  (cr/reset))

(defn restart-dev []
  (stop-dev)
  (start-dev)
  :restarted)

(comment

  (start-dev)

  (restart-dev)

  (stop-dev)

  (p/pprint cr/system)

  (p/pprint (-> cr/system :api-server :rabbit-mq-config))

  (keys  (clojure.pprint/pprint (-> cr/system :api-server :service)))

  (pt/response-for
   (-> cr/system :api-server :service ::http/service-fn)
   :get "/consume"
   :headers {}
    ;; a1995316-80ea-4a98-939d-7c6295e4bb46)
   )

  (-> cr/system :api-server :service ::http/service-fn))

