(ns rabbit.interceprors
  (:require [clojure.pprint :as pprint]
            [rabbit.http :as http]
            [io.pedestal.http :as http1]
            [io.pedestal.http.body-params :as bp]
            [ring.util.response :as rr]
            [rabbit.mqueue :as mqueue]))


(def ^{:const true}
  DEFAULT-QUEUE "public-queue")

(def ^{:const true}
  DEFAULT-EXCHANGE-NAME "exchange")

(def ^{:const true}
  DEFAULT-ROUTING-KEY "routing")

(def ^{:const true}
  DEFAULT-TOPIC "topic")


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
            (let [channel (get-in request [:system/rabbit-mq :rabbit-mq-config :channel])]
              (let [{:keys [qname auto-ack]} (:transit-params request)]
                (assoc ctx :response (rr/response {:result (str "  " channel)})))))})

(def producer-interceptor
  {:name ::producer-interceptor
   :enter (fn [{:keys [request] :as ctx}]
            (let [channel (get-in request [:system/rabbit-mq :rabbit-mq-config :channel])
                  {:keys [qname auto-ack]} (:transit-params request)]
              (assoc ctx :response (rr/created {:result (str "  " channel)}))))})



(def declare-queue-interceptor
  {:name ::declare-queue
   :enter (fn [{:keys [request] :as ctx}]
            (let [channel (get-in request [:system/rabbit-mq :rabbit-mq-config :conn])
                  {:keys [ename topic]} (:transit-params request)
                  ename' (or ename DEFAULT-EXCHANGE-NAME)
                  topic' (or topic DEFAULT-TOPIC)]
              (println "---ename-----" (format "[main] Connected. Channel id: %d" (.getChannelNumber channel)))
              (try
                (mqueue/declare-queue channel DEFAULT-EXCHANGE-NAME DEFAULT-TOPIC)
                (assoc ctx :response (rr/created {:result (format "Created queue for the exhange %1 topic %2", topic', ename')}))
                (catch Exception e
                  (assoc ctx :response {:status 500
                                        :body (format "error in creating queue for the exhange %1 topic %2", topic',ename')})))))})





