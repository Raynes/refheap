(ns refheap.server
  (:require [refheap.config :refer [config]]
            [noir.util.middleware :refer [wrap-strip-trailing-slash wrap-canonical-host wrap-force-ssl]]
            [noir.session :refer [wrap-noir-session wrap-noir-flash]]
            [monger.core :as mg]
            [monger.collection :as mc]
            [monger.ring.session-store :refer [monger-store]]
            [compojure.core :refer [defroutes routes ANY]]
            [compojure.handler :refer [api]]
            [compojure.route :refer [not-found resources]]))

(let [uri (get (System/getenv) "MONGOLAB_URI" "mongodb://127.0.0.1/refheap_development")]
  (mg/connect-via-uri! uri))

(mc/ensure-index "pastes" {:user 1 :date 1})
(mc/ensure-index "pastes" {:private 1})
(mc/ensure-index "pastes" {:id 1})
(mc/ensure-index "pastes" {:paste-id 1})

;; View loading has to be done after mongo is available.
(require '[refheap.views.common :refer [layout]]
         '[refheap.views.about :refer [about-routes]]
         '[refheap.views.legal :refer [legal-routes]]
         '[refheap.views.paste :refer [paste-routes]]
         '[refheap.views.users :refer [user-routes]]
         '[refheap.views.api :refer [api-routes]]
         '[refheap.views.home :refer [home-routes]]
         '[refheap.views.login :refer [login-routes]])

(defn four-zero-four []
  (layout "<p class=\"header\">Insert fancy 404 image here.</p>"))

(defn wrap-prod-middleware [routes]
  (if (System/getenv "LEIN_NO_DEV")
    (-> routes
        (wrap-canonical-host (System/getenv "CANONICAL_HOST"))
        (wrap-force-ssl))
    routes))

(def site-routes
  (-> (routes about-routes
              legal-routes
              paste-routes
              user-routes
              home-routes
              login-routes) 
      (api)
      (wrap-noir-flash)
      (wrap-noir-session {:store (monger-store "sessions")}) 
      (wrap-strip-trailing-slash)
      (wrap-prod-middleware)))

(def a-routes
  (-> api-routes
      (api)
      (wrap-noir-session {:store (monger-store "sessions")})
      (wrap-strip-trailing-slash)
      (wrap-prod-middleware)))

(defroutes handler
  (ANY "*" [] site-routes)
  (ANY "*" [] a-routes)
  (resources "/")
  (not-found (four-zero-four)))
