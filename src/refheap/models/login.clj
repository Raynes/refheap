(ns refheap.models.login
  (:use [refheap.config :only [config]]
        [noir.request :only [ring-request]])
  (:require [somnium.congomongo :as mongo]
            [clj-http.client :as http]
            [noir.session :as session]
            [cheshire.core :as json]))

(defn create-user [email name]
  (let [name (.toLowerCase name)
        qmap {:email email
              :username name}]
    (when-not (mongo/fetch-one :users :where {:username name})
      (mongo/insert!
       :users
       qmap)
      (session/put! :user qmap))))

(defn user-exists [email]
  (when-let [user (:username
                   (mongo/fetch-one
                    :users
                    :where {:email email}))]
    (session/put! :user {:email email
                         :username user})
    user))

(defn verify-host [hosts]
  (hosts (first (.split (get-in (ring-request) [:headers "host"]) ":"))))

(defn verify-assertion [assertion]
  (let [verified (json/parse-string
                  (:body
                   (http/post "https://browserid.org/verify"
                              {:form-params {:assertion assertion
                                             :audience (verify-host (:hosts config))}}))
                  true)]
    (when (= "okay" (:status verified))
      verified)))