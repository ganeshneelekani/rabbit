(ns rabbit.routes
  (:require [clojure.pprint :as pprint]
            [clojure.set :as set]
            [clojure.tools.logging :as log]
            [io.pedestal.http.route :as route]
            [rabbit.interceprors :as inter]))


(defn respond-rabbit-mq [request]
  (clojure.pprint/pprint (get-in request [:system/rabbit-mq :rabbit-mq-config :conn]))
  {:status 200
   :body "Hello, Rabbit MQ!"})

(defn not-found-handler [_]
  {:status 404
   :body "Page not found"})


(def producer-route
  #{["/publish" :post respond-rabbit-mq :route-name :publish]})

;; (http/web-interceptors inter/consumer)
(def consumer-route
  #{["/consume" :post inter/consumer :route-name :consume]})

(def no-routes
  {:not-found {:handler not-found-handler}})

(defn routes []
  (route/expand-routes
   (set/union producer-route consumer-route)))
