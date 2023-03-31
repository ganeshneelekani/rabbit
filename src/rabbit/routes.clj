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

(def producer-route
  #{["/publish" :post (http/web-interceptors inter/publisher-interceptor) :route-name :publish]
    ["/declare-exchange" :post (http/web-interceptors inter/declare-exhange-interceptor) :route-name :declare-exchane]})

(def consumer-route
  #{["/consume" :post (http/web-interceptors inter/consumer-interceptor) :route-name :consume]})


(defn routes []
  (route/expand-routes
   (set/union producer-route consumer-route)))
