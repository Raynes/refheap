(ns refheap.server
  (:require [refheap.config :refer [config]]
            [noir.server :as server]
            [noir.trailing-slash :refer [wrap-strip-trailing-slash]]
            [noir.canonical-host :refer [wrap-canonical-host]]
            [noir.force-ssl :refer [wrap-force-ssl]]
            [monger.core :as mg]
            [monger.collection :as mc]
            [monger.ring.session-store :refer [monger-store]]))

(let [uri (get (System/getenv) "MONGOLAB_URI" "mongodb://127.0.0.1/refheap_development")]
  (mg/connect-via-uri! uri))

(mc/ensure-index "pastes" {:user 1 :date 1})
(mc/ensure-index "pastes" {:private 1})
(mc/ensure-index "pastes" {:id 1})
(mc/ensure-index "pastes" {:paste-id 1})

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
                        :session-store (monger-store "sessions")})))

