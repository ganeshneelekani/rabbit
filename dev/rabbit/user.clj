(ns rabbit.user
  (:require [clojure.pprint :as p]
            [cognitect.transit :as transit]
            [com.stuartsierra.component.repl :as cr]
            [io.pedestal.http :as http]
            [io.pedestal.test :as pt]
            [langohr.channel   :as lch]
            [rabbit.config :as c]
            [rabbit.server :as server])
  (:import (java.io ByteArrayInputStream ByteArrayOutputStream)))


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


  (defn create-request [api body]
    (pt/response-for
     (-> cr/system :api-server :service ::http/service-fn)
     :post api
     :headers {"Content-Type" "application/transit+json"}
     :body (transit-write body)))


  ;; ch qname f auto-ack
  (pt/response-for
   (-> cr/system :api-server :service ::http/service-fn)
   :post "/consume"
   :headers {"Content-Type" "application/transit+json"}
    ;; a1995316-80ea-4a98-939d-7c6295e4bb46).
   :body (transit-write {:ch "channel"
                         :qname "true"
                         :auto-ack "true"}))

  (restart-dev)

  (pt/response-for
   (-> cr/system :api-server :service ::http/service-fn)
   :post "/publish"
   :headers {"Content-Type" "application/transit+json"}
   :body (transit-write {:ename "exchange1"
                         :r-key "r-key1"
                         :payload " My data"}))

  (create-request "/declare-exchange" {:ename "exchange2"
                                       :etype "direct"})

  (create-request "/declare-queue" {:qname "queue4"})

  (create-request "/bind" {:qname "queue4"
                           :ename "exchange1"})

  (create-request "/publish" {:ename "exchange1"
                              :r-key "r-key1"
                              :payload " My data"})

  (create-request "/consume" {:ch "channel"
                              :qname "true"
                              :auto-ack "true"})



  (restart-dev)



;;  (-> cr/system :api-server :service :system/rabbit-mq :rabbit-mq-config :conn)

  (lch/open (-> cr/system :api-server :rabbit-mq-config :rabbit-mq-config :conn))

  (lch/open? (-> cr/system :api-server :rabbit-mq-config :rabbit-mq-config :conn))


  (-> (transit-write {:ch "channel"
                      :qname "true"
                        ;;  :f (fn [_]
                        ;;       identity)
                      :auto-ack true})
      (transit-read))




  ;;
  )

