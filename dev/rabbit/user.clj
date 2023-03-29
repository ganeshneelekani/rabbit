(ns rabbit.user
  (:require [rabbit.config :as c]
            [rabbit.server :as server]
            [com.stuartsierra.component :as component]
            [rabbit.routes :as r]
            [io.pedestal.http :as http]
            [com.stuartsierra.component.repl :as cr]
            [io.pedestal.test :as pt]
            [cognitect.transit :as transit]
            [clojure.pprint :as p])
  (:import (java.io ByteArrayOutputStream ByteArrayInputStream)))


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

(defn transit-write [obj]
  (let [out (ByteArrayOutputStream.)
        writer (transit/writer out :json)]
    (transit/write writer obj)
    (.toString out)))

(defn transit-read [txt]
  (let [in (ByteArrayInputStream. (.getBytes txt))
        reader (transit/reader in :json)]
    (transit/read reader)))


(comment

  (start-dev)

  (restart-dev)

  (stop-dev)

  (p/pprint cr/system)

  (p/pprint (-> cr/system :api-server :rabbit-mq-config))

  (keys  (clojure.pprint/pprint (-> cr/system :api-server :service)))

  ;; ch qname f auto-ack
  (pt/response-for
   (-> cr/system :api-server :service ::http/service-fn)
   :post "/consume"
   :headers {"Content-Type" "application/transit+json"}
    ;; a1995316-80ea-4a98-939d-7c6295e4bb46).
   :body (transit-write {:ch "channel"
                         :qname "true"
                        ;;  :f (fn [_]
                        ;;       identity)
                         :auto-ack true}))

  (-> cr/system :api-server :service ::http/service-fn)


  (-> (transit-write {:ch "channel"
                      :qname "true"
                        ;;  :f (fn [_]
                        ;;       identity)
                      :auto-ack true})
      (transit-read))




  ;;
  )

