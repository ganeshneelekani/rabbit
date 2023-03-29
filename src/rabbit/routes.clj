(ns rabbit.routes
  (:require [io.pedestal.http.route :as route]
            [clojure.pprint :as pprint]
            [clojure.tools.logging :as log]
            [clojure.set :as set]))


(defn respond-rabbit-mq [request]
  ;;(clojure.pprint/pprint (get-in request [:system/rabbit-mq :rabbit-mq-config :conn]))
  {:status 200
   :body "Hello, Rabbit MQ!"})

(defn not-found-handler [_]
  {:status 404
   :body "Page not found"})


(def producer-route
  #{["/publish" :post respond-rabbit-mq :route-name :publish]})

(def consumer-route
  #{["/consume" :get respond-rabbit-mq :route-name :consume]})

(def no-routes
  {:not-found {:handler not-found-handler}})

(defn routes []
  (route/expand-routes
   (set/union producer-route consumer-route)))
