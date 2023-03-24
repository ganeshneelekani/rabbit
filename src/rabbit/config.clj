(ns rabbit.config)

(def rabbit-mq-config {:host (or (System/getenv "RABBIT_MQ_HOST") "localhost")
                       :port (or (System/getenv "RABBIT_MQ_PORT") 5672)
                       :username (or (System/getenv "RABBIT_MQ_USERNAME") "myuser")
                       :password (or (System/getenv "RABBIT_MQ_PASSWORD") "mypassword")})

(def service-map {:env :dev
                  :io.pedestal.http/type :jetty
                  :io.pedestal.http/join? false
                  :io.pedestal.http/port (or (System/getenv "PORT") 3200)})

(def server-config
  {:service-map service-map
   :rabbit-mq-config rabbit-mq-config})