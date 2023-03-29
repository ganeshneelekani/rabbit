(ns rabbit.mqueue
  (:require [langohr.core      :as rmq]
            [langohr.channel   :as lch]
            [langohr.exchange  :as le]
            [langohr.queue     :as lq]
            [langohr.consumers :as lc]
            [langohr.basic     :as lb]))

(def ^{:const true}
  DEFAULT-QUEUE "public-queue")

(def ^{:const true}
  DEFAULT-EXCHANGE-NAME "")

(def ^{:const true}
  DEFAULT-ROUTING-KEY "")

(defn message-handler [f qname ch metadata ^bytes payload]
  {:qname qname
   :channel ch
   :metadata metadata
   :payload (f (String. payload))})

(defn start-consumer
  "Starts a consumer in a separate thread"
  [ch qname f auto-ack]
  (.start (Thread. (fn []
                     (lc/subscribe ch qname (partial message-handler f qname) {:auto-ack auto-ack})))))

(defn bind-channel
  "Binds channel to queue and exchange"
  [ch q ename]
  (lq/bind ch q ename))

(defn declare-queue
  "Declare a queue"
  [ch ename exclusive auto-delete]
  (lq/declare ch (or ename DEFAULT-EXCHANGE-NAME) {:exclusive exclusive
                                                   :auto-delete auto-delete}))

(defn publish-message
  "Publish the message"
  [ch ename r-key payload type]
  (lb/publish ch
              ename
              (or r-key DEFAULT-ROUTING-KEY)
              (or payload "Sample data")
              {:content-type type}))
