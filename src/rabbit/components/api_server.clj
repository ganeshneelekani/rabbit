(ns rabbit.components.api-server
  (:require [rabbit.routes :as routes]
            [langohr.core :as rmq]
            [langohr.channel   :as lch]
            [io.pedestal.http :as http]
            [io.pedestal.interceptor :as interceptor]
            [com.stuartsierra.component :as component]))

(defn dev?
  [service-map]
  (= :dev (:env service-map)))

(defn r-routes
  [service-map]
  (let [routes (if (dev? service-map)
                 #(routes/routes)
                 (routes/routes))]
    (assoc service-map ::http/routes routes)))

(defn inject-system
  [system]
  (interceptor/interceptor
   {:name ::inject-system
    :enter (fn [ctx]
             (update-in ctx [:request] merge system))}))

(defn merge-interceptor
  [service-map sys-interceptors]
  (let [default-interceptors (-> service-map
                                 (http/default-interceptors)
                                 ::http/interceptors)
        interceptors (into [] (concat
                               (butlast default-interceptors)
                               sys-interceptors
                               [(last default-interceptors)]))]
    (assoc service-map ::http/interceptors interceptors)))

(defn create-server
  [service-map]
  (http/create-server (if (dev? service-map)
                        (http/dev-interceptors service-map)
                        service-map)))

(defrecord  ApiServer [service-map service rabbit-mq-config]
  component/Lifecycle
  (start [component]
    (println ";; Stating API Server ")
    (let [service (-> service-map
                      (r-routes)
                      (merge-interceptor [(inject-system {:system/rabbit-mq rabbit-mq-config})])
                      (create-server)
                      (http/start))]
      (assoc component :service service)))

  (stop [component]
    (println ";; Stopping API server")
    (when service
      (http/stop service))
    (assoc component :service nil)))


(defn service
  [service-map]
  (map->ApiServer {:service-map service-map}))

(defrecord Rabbit-mq [rabbit-mq-config]

  component/Lifecycle

  (start [component]
    (println ";; Starting Rabbit MQ connection ")
    (let [conn  (rmq/connect rabbit-mq-config)
          ch    (lch/open conn)]
      (-> component
          (update-in [:rabbit-mq-config :conn] (constantly conn))
          (update-in [:rabbit-mq-config :channel] (constantly ch)))))

  (stop [component]
    (println ";; Stopping Rabbit MQ connection")
    (-> component
        (update-in [:rabbit-mq-config :conn] (constantly nil))
        (update-in [:rabbit-mq-config :channel] (constantly nil)))))


(defn rabbit-mq-service
  [config]
  (map->Rabbit-mq {:rabbit-mq-config config}))