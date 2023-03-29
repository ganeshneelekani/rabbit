(ns rabbit.interceprors
  (:require [clojure.pprint :as pprint]
            [rabbit.http :as http]
            [io.pedestal.http :as http1]
            [io.pedestal.http.body-params :as bp]))


(defmacro interceptor-> [name handler]
  {:name name
   :enter `(fn [context#]
             (let [request# (:request context#)
                   response# (~handler request#)]
               (assoc context# :response response#)))})

(defn ws-params
  "Get the body parameters regardless of type"
  [{json-params :json-params
    edn-params :edn-params
    form-params :form-params}]
  (or json-params edn-params form-params))


(defn http-interceptor [id fnc]
  (interceptor->
   id
   (fn [ctx]
     (fnc ctx))))

(def consumer
  (http/web-interceptors
   (http-interceptor
    ::consumer-interceptor
    (fn [ctx]
      (println "----12------" (keys ctx))
      (pprint/pprint ctx)
      (let [{:keys [ch qname f auto-ack]} (ws-params ctx)]
        {:status 200
         :body   (str "ABC" ch qname f auto-ack)})))))