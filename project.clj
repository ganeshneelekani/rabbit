(defproject rabbit "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [com.stuartsierra/component "0.4.0"]
                 [io.pedestal/pedestal.service "0.5.11-beta-1"]
                 [io.pedestal/pedestal.service-tools "0.5.11-beta-1"]
                 [io.pedestal/pedestal.jetty "0.5.11-beta-1"]
                 [io.pedestal/pedestal.route "0.5.11-beta-1"]
                 [ring/ring-json "0.5.1"]
                 [com.novemberain/langohr "5.1.0"]]
  :main ^:skip-aot rabbit.server
  :repl-options {:init-ns rabbit.user}
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}
             :dev {:source-paths ["dev/src"]
                   :resource-paths ["dev/resources"]
                   :dependencies [[com.stuartsierra/component.repl "1.0.0"]]}}
  ;; lein with-profile dev repl
  :uberjar-name "rabbit.jar")
