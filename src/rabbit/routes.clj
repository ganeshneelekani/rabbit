(ns rabbit.routes
  (:require [io.pedestal.http.route :as route]
            [clojure.pprint :as pprint]))

(defn respond-rabbit-mq [request]
  (clojure.pprint/pprint (get-in request [:system/rabbit-mq :rabbit-mq-config :conn]))
  {:status 200
   :body "Hello, Rabbit MQ!"})

(defn routes
  []
  (route/expand-routes
   #{["/test" :get respond-rabbit-mq :route-name :test]}))