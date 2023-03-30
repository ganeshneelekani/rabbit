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
  DEFAULT-EXCHANGE-NAME "exchange")

(def ^{:const true}
  DEFAULT-ROUTING-KEY "routing")

(def ^{:const true}
  DEFAULT-TOPIC "topic")

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
  [conn ename topic & {:keys [durable auto-delete exclusive]
                       :or   {durable     false
                              auto-delete true
                              exclusive   false}}]
  (let [ch (lch/open conn)]
    (le/declare ch ename topic
                {:durable     durable
                 :auto-delete auto-delete
                 :exclusive   exclusive})
    (lch/close ch)))

(defn publish-message
  "Publish the message"
  [ch ename r-key payload type]
  (lb/publish ch ename r-key
              (or payload "Sample data")
              {:content-type type}))
