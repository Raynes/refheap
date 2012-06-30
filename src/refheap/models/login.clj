(ns refheap.models.login
  (:require [refheap.config :refer [config]]
            [clj-http.client :as http]
            [noir.session :as session]
            [cheshire.core :as json]
            [refheap.messages :refer [error]]
            [noir.request :refer [ring-request]]
            [monger.collection :as mc])
  (:import org.bson.types.ObjectId))

(defn create-user [email name]
  (let [name (.toLowerCase name)
        qmap {:email email
              :username name}
        name-count (count name)]
    ;; this stuff would be much cleaner with Validateur. MK.
    (cond
     (or (> 3 name-count) (< 15 name-count)) 
     (error "Username must be between 3 and 15 characters.")
     (not= name (first (re-seq #"\w+" name)))
     (error "Username cannot contain non-alphanumeric characters.")
     (mc/find-one-as-map "users" {:username name})
     (error "Username already exists.")
     :else (let [oid  (ObjectId.)
                 user (merge {:_id oid} qmap)]
             (mc/insert "users" user)
             (session/put! :user (assoc qmap :id (str (:_id user))))))))

(defn user-exists [email]
  (when-let [{:keys [username _id]} (mc/find-one-as-map "users" {:email email})]
    (session/put! :user {:email email
                         :username username
                         :id (str _id)})
    username))

(defn verify-host [hosts]
  (hosts (first (.split (get-in (ring-request) [:headers "host"]) ":"))))

(defn get-hosts []
  (if-let [hosts (System/getenv "HOSTS")]
    (set (.split hosts ","))
    (or (:hosts config) #{"localhost"})))

(defn verify-assertion [assertion]
  (let [verified (json/parse-string
                  (:body
                   (http/post "https://browserid.org/verify"
                              {:form-params
                               {:assertion assertion
                                :audience (verify-host (get-hosts))}}))
                  true)]
    (when (= "okay" (:status verified))
      verified)))