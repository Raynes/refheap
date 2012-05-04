(ns refheap.models.login
  (:use [refheap.messages :only [error]]
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
     :else (let [user (mongo/insert!
                       :users
                       qmap)]
             (session/put! :user (assoc qmap :id (str (:_id user))))))))

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
  (let [hosts (System/getenv "HOSTS")
        verified (json/parse-string
                  (:body
                   (http/post "https://browserid.org/verify"
                              {:form-params
                               {:assertion assertion
                                :audience (verify-host
                                           (if hosts
                                             (.split (System/getenv "HOSTS") ",")
                                             ["localhost"]))}}))
                  true)]
    (when (= "okay" (:status verified))
      verified)))