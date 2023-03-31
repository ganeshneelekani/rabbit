(ns rabbit.mqueue
  (:require [langohr.basic     :as lb]
            [langohr.channel   :as lch]
            [langohr.consumers :as lc]
            [langohr.exchange  :as le]
            [langohr.queue     :as lq]))


(def ^{:const true}
  DEFAULT-QUEUE "public-queue")

(def ^{:const true}
  DEFAULT-EXCHANGE-NAME "exchange")

(def ^{:const true}
  DEFAULT-ROUTING-KEY "routing")

(def ^{:const true}
  DEFAULT-TOPIC "topic")

(def ^{:const true}
  DEFAULT-PAYLOAD "Sample payload")

(def ^{:const true}
  DEFAULT-ETYPE "direct")

(defn message-handler [ch {:keys [routing-key] :as meta} ^bytes payload]
  {;;:metadata meta
   :routing-key routing-key
   :payload (String. payload)})

(defn bind-channel
  "Binds channel to queue and exchange"
  [ch q ename topic-name]
  (lq/bind ch q ename {:routing-key topic-name}))

(defn start-consumer
  "Starts a consumer in a separate thread"
  [conn qname & {:keys [exclusive auto-delete auto-ack]
                 :or   {exclusive     false
                        auto-delete true
                        auto-ack true}}]
  (let [ch (lch/open conn)]
    (lc/subscribe ch qname message-handler {:auto-ack auto-ack})))

(defn declare-exchange
  "Declare a exchange"
  [conn ename etype & {:keys [durable auto-delete internal]
                       :or   {durable     false
                              auto-delete true
                              internal   false}}]
  (let [ch (lch/open conn)]
    (le/declare ch ename etype
                {:durable     durable
                 :auto-delete auto-delete
                 :internal   internal})
    (lch/close ch)))


(defn declare-queue
  "Declare a queue"
  [conn qname & {:keys [durable auto-delete exclusive]
                 :or   {durable     false
                        auto-delete true
                        exclusive   false}}]
  (let [ch (lch/open conn)
        q (-> (lq/declare ch qname
                          {:durable     durable
                           :auto-delete auto-delete
                           :exclusive   exclusive})
              :queue)]
    (lch/close ch)
    q))

(defn bind
  "Binds a queue to an exchange"
  [conn q ename & {:keys [routing-key]
                   :or   {routing-key DEFAULT-ROUTING-KEY}}]
  (let [ch (lch/open conn)]
    (lq/bind ch q ename {:routing-key routing-key})
    (lch/close ch)))

(defn publish-message
  "Publish the message"
  [conn ename r-key payload & {:keys [content-type type]
                               :or   {content-type "text/plain"
                                      type   "type"}}]
  (let [ch (lch/open conn)]
    (lb/publish ch ename r-key payload {:content-type "text/plain"
                                        :type "greetings.hi"
                                        :correlation-id "123"
                                        :message-id "345"})
    (lch/close ch)))
