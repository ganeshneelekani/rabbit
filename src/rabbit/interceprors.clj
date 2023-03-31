(ns rabbit.interceprors
  (:require [clojure.pprint :as pprint]
            [rabbit.http :as http]
            [io.pedestal.http :as http1]
            [io.pedestal.http.body-params :as bp]
            [ring.util.response :as rr]
            [rabbit.mqueue :as mqueue]))

(defn ws-params
  "Get the body parameters regardless of type"
  [{json-params :json-params
    edn-params :edn-params
    form-params :form-params}]
  (or json-params edn-params form-params))


(def consumer-interceptor
  {:name ::consumer-interceptor
   :enter (fn [{:keys [request] :as ctx}]
            (let [conn (get-in request [:system/rabbit-mq :rabbit-mq-config :conn])
                  {:keys [topic qname ename]} (:transit-params request)
                  ename' (or ename mqueue/DEFAULT-EXCHANGE-NAME)
                  topic' (or topic mqueue/DEFAULT-TOPIC)
                  qname' (or qname mqueue/DEFAULT-QUEUE)]
              (try
                (mqueue/start-consumer conn topic' ename' qname')
                (assoc ctx :response (rr/response  "Message published"))
                (catch Exception e
                  (println e)
                  (assoc ctx :response {:status 500
                                        :body (format "error in publishing message for the topic  %s qname %s", topic',qname')})))))})

(def publisher-interceptor
  {:name ::publisher-interceptor
   :enter (fn [{:keys [request] :as ctx}]
            (clojure.pprint/pprint request)
            (let [conn (get-in request [:system/rabbit-mq :rabbit-mq-config :conn])
                  {:keys [ename r-key payload]} (:transit-params request)
                  ename' (or ename mqueue/DEFAULT-EXCHANGE-NAME)
                  r-key' (or r-key mqueue/DEFAULT-ROUTING-KEY)
                  payload' (or payload mqueue/DEFAULT-PAYLOAD)]
              (try
                (mqueue/publish-message conn ename' r-key' payload')
                (assoc ctx :response (rr/response  "Message published"))
                (catch Exception e
                  (println e)
                  (assoc ctx :response {:status 500
                                        :body (format "error in publishing message for the exhange name %s routing key %s", ename',r-key')})))))})

(def declare-exhange-interceptor
  {:name ::declare-exchange
   :enter (fn [{:keys [request] :as ctx}]
            (let [conn (get-in request [:system/rabbit-mq :rabbit-mq-config :conn])
                  {:keys [ename etype]} (:transit-params request)
                  ename' (or ename mqueue/DEFAULT-EXCHANGE-NAME)
                  etype' (or etype mqueue/DEFAULT-ETYPE)]
              (try
                (mqueue/declare-exchange conn ename' etype')
                (assoc ctx :response (rr/created "Message exchange is created"))
                (catch Exception e
                  (println "---2---" e)
                  (assoc ctx :response {:status 500
                                        :body (format "error in creating queue for the exhange %s topic %s", etype',ename')})))))})





