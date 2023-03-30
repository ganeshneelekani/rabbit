(ns rabbit.interceprors
  (:require [clojure.pprint :as pprint]
            [rabbit.http :as http]
            [io.pedestal.http :as http1]
            [io.pedestal.http.body-params :as bp]
            [ring.util.response :as rr]
            [rabbit.mqueue :as mqueue]))


(defmacro interceptor-> [name handler]
  {:name name
   :enter `(fn [context#]
             (let [request# (:request context#)
                   response# (~handler request#)]
               (assoc context# :response response#)))})

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

(def declare-queue-interceptor
  {:name ::declare-queue
   :enter (fn [{:keys [request] :as ctx}]
            (let [conn (get-in request [:system/rabbit-mq :rabbit-mq-config :conn])
                  {:keys [ename topic]} (:transit-params request)
                  ename' (or ename mqueue/DEFAULT-EXCHANGE-NAME)
                  topic' (or topic mqueue/DEFAULT-TOPIC)]
              (try
                (mqueue/declare-queue conn ename' topic')
                (assoc ctx :response (rr/created nil "Message queue is created"))
                (catch Exception e
                  (assoc ctx :response {:status 500
                                        :body (format "error in creating queue for the exhange %s topic %s", topic',ename')})))))})





