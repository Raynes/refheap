(ns refheap.server
  (:use [refheap.config :only [config]]
        [mongo-session.core :only [mongo-session]])
  (:require [noir.server :as server]
            [somnium.congomongo :as mongo]))

(mongo/set-connection!
 (mongo/make-connection (config :db-name)
                        :host (config :db-host)
                        :port (config :db-port)))

(mongo/add-index! :pastes [:user :date])
(mongo/add-index! :pastes [:private])

(server/load-views "src/refheap/views/")

(defn -main [& m]
  (let [mode (keyword (or (first m) :dev))
        port (Integer. (get (System/getenv) "PORT" "8080"))]
    (server/start port {:mode mode
                        :ns 'refheap
                        :session-store (mongo-session :sessions)})))

