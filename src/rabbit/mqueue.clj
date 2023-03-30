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

(def ^{:const true}
  DEFAULT-PAYLOAD "Sample payload")

(defn message-handler [qname ch metadata ^bytes payload]
  {:qname qname
   :channel ch
   :metadata metadata
   :payload (String. payload)})

(defn bind-channel
  "Binds channel to queue and exchange"
  [ch q ename topic-name]
  (lq/bind ch q ename {:routing-key topic-name}))

(defn start-consumer
  "Starts a consumer in a separate thread"
  [conn topic ename qname & {:keys [exclusive auto-delete auto-ack]
                             :or   {exclusive     false
                                    auto-delete true
                                    auto-ack true}}]
  (let [ch (lch/open conn)]
    (bind-channel ch qname ename topic)
    (.start (Thread. (fn []
                       (lc/subscribe ch qname (partial message-handler qname) {:auto-ack auto-ack}))))))

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
  [conn ename r-key payload & {:keys [content-type type]
                               :or   {content-type "text/plain"
                                      type   "type"}}]
  (let [ch (lch/open conn)]
    (println "----ch------" ch)
    (println "----1------" ename "  " r-key "   " payload)
    (lb/publish ch ename r-key payload {:content-type content-type
                                        :type type})
    (lch/close ch)))
