(ns rabbit.routes
  (:require [clojure.pprint :as pprint]
            [clojure.set :as set]
            [clojure.tools.logging :as log]
            [io.pedestal.http.route :as route]
            [rabbit.interceprors :as inter]
            [rabbit.http :as http]))


(defn respond-rabbit-mq [request]
  (clojure.pprint/pprint request)
  {:status 200
   :body "Hello, Rabbit MQ!"})

(defn not-found-handler [_]
  {:status 404
   :body "Page not found"})
;;declare-queue

(def producer-route
  #{["/publish" :post respond-rabbit-mq :route-name :publish]
    ["/declare" :post inter/declare-queue-interceptor :route-name :declare-queue]})

;; (http/web-interceptors in.zter/consumer)
(def consumer-route
  #{["/consume" :post (http/web-interceptors inter/consumer-interceptor) :route-name :consume]})

(def no-routes
  {:not-found {:handler not-found-handler}})

(defn routes []
  (route/expand-routes
   (set/union producer-route consumer-route)))
