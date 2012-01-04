(ns refheap.models.login
  (:use [refheap.config :only [config]]
        [refheap.messages :only [error]]
        [noir.request :only [ring-request]])
  (:require [somnium.congomongo :as mongo]
            [clj-http.client :as http]
            [noir.session :as session]
            [cheshire.core :as json]))

(defn create-user [email name]
  (let [name (.toLowerCase name)
        qmap {:email email
              :username name}
        name-count (count name)]
    (cond
     (or (> 3 name-count) (< 15 name-count)) 
     (error "Username must be between 3 and 15 characters.")
     (not= name (first (re-seq #"\w+" name)))
     (error "Username cannot contain non-alphanumeric characters.")
     (mongo/fetch-one :users :where {:username name})
     (error "Username already exists.")
     :else (do (mongo/insert!
                :users
                qmap)
               (session/put! :user qmap)))))

(defn user-exists [email]
  (when-let [{:keys [username _id]} (mongo/fetch-one
                                     :users
                                     :where {:email email})]
    (session/put! :user {:email email
                         :username username
                         :id (str _id)})
    username))

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