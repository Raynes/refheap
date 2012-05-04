(ns refheap.server
  (:require [refheap.config :refer [config]]
            [mongo-session.core :refer [mongo-session]]
            [noir.response :refer [redirect]])
  (:require [noir.server :as server]
            [somnium.congomongo :as mongo]))

(defn mongolab-info []
  "Parse mongodb uri from mongolab on heroku, eg.
  mongodb://user:pass@localhost:1234/db"
  (let [matcher (re-matcher #"^.*://(.*?):(.*?)@(.*?):(\d+)/(.*)$"
                            (System/getenv "MONGOLAB_URI"))]
    (when (.find matcher)
      (zipmap [:match :user :pass :host :port :db] (re-groups matcher)))))

(let [{:keys [db port host user pass]} (mongolab-info)
      connection (mongo/make-connection (or db (config :db-name)) 
                                        :host (or host (config :db-host)) 
                                        :port (Integer. (or port (config :db-port))))]
  (when (and user pass)
    (mongo/authenticate connection user pass))
  (mongo/set-connection! connection))

(mongo/add-index! :pastes [:user :date])
(mongo/add-index! :pastes [:private])
(mongo/add-index! :pastes [:id])
(mongo/add-index! :pastes [:paste-id])

(server/load-views "src/refheap/views/")

(defn wrap-force-ssl [app]
  (fn [req]
    (let [headers (:headers req)]
      (if (or (= :https (:scheme req))
              (= "https" (headers "x-forwarded-proto")))
        (app req)
        (redirect (str "https://" (headers "host") (:uri req)))))))

(defn -main [& m]
  (let [mode (keyword (or (first m) :dev))
        port (Integer. (or (get (System/getenv) "PORT") (str (config :port))))]
    (when (= mode :prod)
      (server/add-middleware wrap-force-ssl))
    (server/start port {:mode mode
                        :ns 'refheap
                        :session-store (mongo-session :sessions)})))

