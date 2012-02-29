(ns refheap.server
  (:use [refheap.config :only [config]]
        [mongo-session.core :only [mongo-session]])
  (:require [noir.server :as server]
            [somnium.congomongo :as mongo]))

(defn mongolab-info []
  "Parse mongodb uri from mongolab on heroku, eg.
  mongodb://user:pass@localhost:1234/db"
  (let [matcher (re-matcher #"^.*://(.*?):(.*?)@(.*?):(\d+)/(.*)$"
                            (System/getenv "MONGOLAB_URI"))]
    (when (.find matcher)
      (zipmap [:match :user :pass :host :port :db] (re-groups matcher)))))

(let [{:keys [db port host]} (mongolab-info)]
  (mongo/set-connection!
    (mongo/make-connection (or db (config :db-name)) 
                           :host (or host (config :db-host)) 
                           :port (Integer. (or port (config :db-port)))))) 

(mongo/add-index! :pastes [:user :date])
(mongo/add-index! :pastes [:private])
(mongo/add-index! :pastes [:id])
(mongo/add-index! :pastes [:paste-id])

(server/load-views "src/refheap/views/")

(defn -main [& m]
  (let [mode (keyword (or (first m) :dev))
        port (Integer. (or (get (System/getenv) "PORT") (str (config :port))))]
    (server/start port {:mode mode
                        :ns 'refheap
                        :session-store (mongo-session :sessions)})))

