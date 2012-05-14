(ns refheap.middleware
  (:require [noir.response :refer [redirect]]
            [clojure.string :as s]))

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

(defn wrap-strip-trailing-slash [handler]
  (fn [request]
    (handler (update-in request [:uri] s/replace #"(?<=.)/$" ""))))
