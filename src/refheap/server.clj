(ns refheap.server
  (:require [refheap.config :refer [config]]
            [noir.server :as server]
            [noir.trailing-slash :refer [wrap-strip-trailing-slash]]
            [noir.canonical-host :refer [wrap-canonical-host]]
            [noir.force-ssl :refer [wrap-force-ssl]]
            [monger.ring.session-store :refer [monger-store]]))

(defn mongolab-info []
  (System/getenv "MONGOLAB_URI"))

(let [{:keys [path port host user-info]} (mongolab-info)
      [user pass] (and user-info (.split user-info ":"))
      connection (mongo/make-connection (if path (subs path 1) (config :db-name)) 
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
(server/add-middleware wrap-strip-trailing-slash)

(defn -main [& m]
  (let [mode (keyword (or (first m) :dev))
        port (Integer. (or (System/getenv "PORT") (str (config :port))))]
    (when (= mode :prod)
      (server/add-middleware wrap-canonical-host (System/getenv "CANONICAL_HOST"))
      (server/add-middleware wrap-force-ssl))
    (server/start port {:mode mode
                        :ns 'refheap
                        :session-store (monger-store :sessions)})))

