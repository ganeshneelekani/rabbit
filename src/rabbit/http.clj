(ns rabbit.http
  (:require  [io.pedestal.http :as http]
             [io.pedestal.http.content-negotiation :as conneg]
             [io.pedestal.interceptor.error :as error-int]
             [io.pedestal.http.cors :as cors]
             [clojure.data.json :as json]
             [io.pedestal.http.body-params :as bp]
             [ring.util.codec :as ringutil]
             [clojure.walk :refer [postwalk]]
             [clojure.pprint :as pprint])
  (:import (java.util UUID)))

(def http-error-handler
  (error-int/error-dispatch
   [ctx ex]
   [{:exception-type :clojure.lang.ExceptionInfo}]
   (assoc ctx :response {:status 400
                         :body   (str (.getMessage ex) (ex-data ex))})
   :else
   (assoc ctx :response {:status 400
                         :body   (str (.getClass ex) (.getMessage ex))})
                            ;(assoc ctx :io.pedestal.interceptor.chain/error ex))
   ))

(defn custom-serialize [_ v]
  (if (instance? UUID v) (str "#uuid-" v) v))

(defn serialize-seqs-out [body]
  (postwalk (fn [x] (if (set? x)
                      (set (map
                            #(custom-serialize nil %)
                            x))
                      x))
            body))

(defn transform-content
  [body content-type]
  (case content-type
    "application/edn" (pr-str body)
    "application/json" (json/write-str (serialize-seqs-out body) :value-fn custom-serialize)))

(defn accepted-type
  [context]
  (get-in context [:request :accept :field] "application/edn"))

(defn coerce-to
  [response content-type]
  (-> response
      (update :body transform-content content-type)
      (assoc-in [:headers "Content-Type"] content-type)))

(def coerce-body
  {:name ::coerce-body
   :leave
   (fn [context]
     (if (get-in context [:response :headers "Content-Type"])
       context
       (update-in context [:response] coerce-to (accepted-type context))))})

(def def-content-type
  {:name :def-content-type
   :enter
   (fn [ctx]
     (let [content-type (not-empty (or (get-in ctx [:request :content-type]) (get-in ctx [:request :headers "content-type"])))
           resp (if-not content-type (-> ctx (assoc-in [:request :content-type] "application/edn") (assoc-in [:request :headers "content-type"] "application/edn")) ctx)]
       resp))})

(defn url-decode [url]
  (ringutil/url-decode url))


(def url-decode-path-params
  {:name :url-decode-path-params
   :enter
   (fn [ctx]
     (let [path-params (get-in ctx [:request :path-params])
           resp (if path-params
                  (assoc-in ctx [:request :path-params] (into {} (map (fn [[k v]] [k (url-decode v)]) path-params)))
                  ctx)]
       resp))})

(defn csp-fn [ctx]
  (update-in ctx [:response :headers]
             #(assoc % "Cache-Control" "no-cache,no-store,must-revalidate"
                     "Pragma" "no-cache"
                     "X-Frame-Options" "DENY"
                     "Strict-Transport-Security" "max-age=1800"
                     "X-XSS-Protection" "1; mode=block"
                     "X-Content-Type-Options" "nosniff")))

(def csp-interceptor
  {:name :csp-add
   :leave csp-fn})

(def supported-types ["application/edn"
                      "application/json"
                      "application/x-www-form-urlencoded"
                      "application/transit+json"])

(def content-neg-intc
  (conneg/negotiate-content supported-types
                            {:no-match-fn
                             (fn [ctx]
                               (assoc-in ctx [:request :accept] "application/edn"))}))


(def common-interceptor
  [http/transit-body http-error-handler coerce-body def-content-type url-decode-path-params content-neg-intc
   (bp/body-params) csp-interceptor])


(defn web-interceptors
  "Generate a vector of standard interceptors for a web service (content negotiation, parsing, etc)"
  ([interceptor]
   (vec (concat common-interceptor [interceptor])))
  ([interceptor & interceptors]
   (vec (concat [common-interceptor] (concat [interceptor] interceptors)))))