(ns refheap.server
  (:require [refheap.config :refer [config]]
            [mongo-session.core :refer [mongo-session]]
            [noir.response :refer [redirect]]
            [noir.server :as server]
            [somnium.congomongo :as mongo]
            [org.bovinegenius.exploding-fish :as uri]))

(defn mongolab-info []
  (when-let [env (System/getenv "MONGOLAB_URI")]
    (uri/uri env)))

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

(defn wrap-force-ssl [app]
  (fn [req]
    (let [headers (:headers req)]
      (if (or (= :https (:scheme req))
              (= "https" (headers "x-forwarded-proto")))
        (app req)
        (redirect (str "https://" (headers "host") (:uri req)) :permanent)))))

(defn wrap-canonical-host [app]
  (fn [req]
    (let [headers (:headers req)
          canonical (System/getenv "CANONICAL_HOST")]
      (when canonical
        (if (= (headers "host") canonical)
          (app req)
          (redirect (str "https://" canonical (:uri req)) :permanent))))))

(defn -main [& m]
  (let [mode (keyword (or (first m) :dev))
        port (Integer. (or (get (System/getenv) "PORT") (str (config :port))))]
    (when (= mode :prod)
      (server/add-middleware wrap-canonical-host)
      (server/add-middleware wrap-force-ssl))
    (server/start port {:mode mode
                        :ns 'refheap
                        :session-store (mongo-session :sessions)})))

