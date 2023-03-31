(ns rabbit.interceprors
  (:require [rabbit.mqueue :as mqueue]
            [ring.util.response :as rr]))

(defn ws-params
  "Get the body parameters regardless of type"
  [{json-params :json-params
    edn-params :edn-paramsS
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
                  (assoc ctx :response (rr/status {:body (format "error in publishing message for the topic  %s qname %s", topic',qname')} 500))))))})

(def publisher-interceptor
  {:name ::publisher-interceptor
   :enter (fn [{:keys [request] :as ctx}]
            (let [conn (get-in request [:system/rabbit-mq :rabbit-mq-config :conn])
                  {:keys [ename r-key payload]} (:transit-params request)
                  ename' (or ename mqueue/DEFAULT-EXCHANGE-NAME)
                  r-key' (or r-key mqueue/DEFAULT-ROUTING-KEY)
                  payload' (or payload mqueue/DEFAULT-PAYLOAD)]
              (try
                (mqueue/publish-message conn ename' r-key' payload')
                (assoc ctx :response (rr/response  "Message published"))
                (catch Exception e
                  (assoc ctx :response (rr/status {:body (format "error in publishing message for the exhange name %s routing key %s", ename',r-key')} 500))))))})

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
                  (assoc ctx :response (rr/status {:body (format "error in creating queue for the exhange %s topic %s",  etype',ename')} 500))))))})

(def declare-queue-interceptor
  {:name ::declare-queue
   :enter (fn [{:keys [request] :as ctx}]
            (let [conn (get-in request [:system/rabbit-mq :rabbit-mq-config :conn])
                  {:keys [qname]} (:transit-params request)
                  qname' (or qname mqueue/DEFAULT-QUEUE)]
              (try
                (let [q (mqueue/declare-queue conn qname')]
                  (assoc ctx :response (rr/response (format "Queue created %s" q))))
                (catch Exception e
                  (assoc ctx :response (rr/status {:body (format "error in creating queue %s",qname')} 500))))))})

(def bind-interceptor
  {:name ::bind
   :enter (fn [{:keys [request] :as ctx}]
            (let [conn (get-in request [:system/rabbit-mq :rabbit-mq-config :conn])
                  {:keys [qname ename]} (:transit-params request)
                  qname' (or qname mqueue/DEFAULT-QUEUE)
                  ename' (or ename mqueue/DEFAULT-EXCHANGE-NAME)]
              (try
                (mqueue/bind conn qname' ename')
                (assoc ctx :response (rr/created (format "Binding queue %s to the exchange %s " qname',ename')))
                (catch Exception e
                  (assoc ctx :response (rr/status {:body (format "error in binding queue %s",qname')} 500))))))})