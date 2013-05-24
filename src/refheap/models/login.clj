(ns refheap.models.login
  (:require [refheap.config :refer [config]]
            [clj-http.client :as http]
            [noir.session :as session]
            [cheshire.core :as json]
            [refheap.messages :refer [error]]
            [monger.collection :as mc]
            [monger.operators :refer [$set]])
  (:import org.bson.types.ObjectId))

(defn transfer-anon-pastes [user]
  (doseq [id (session/get! :anon-pastes)]
    (mc/update "pastes" {:paste-id id} {$set {:user user}}
               :upsert false :multi false)))

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
     :else (let [user (mc/insert-and-return "users" qmap)
                 id (str (:_id user))]
             (transfer-anon-pastes id)
             (session/put! :user (assoc qmap :id id))))))

(defn user-exists [email]
  (when-let [{:keys [username _id]} (mc/find-one-as-map "users" {:email email})]
    (transfer-anon-pastes (str _id))
    (session/put! :user {:email email
                         :username username
                         :id (str _id)})
    username))

(defn verify-host [host hosts]
  (-> (.split host ":")
      (first)
      (hosts)))

(defn get-hosts []
  (if-let [hosts (System/getenv "HOSTS")]
    (set (.split hosts ","))
    (or (:hosts config) #{"localhost"})))

(defn verify-assertion [host assertion]
  (let [verified (json/parse-string
                  (:body
                   (http/post "https://browserid.org/verify"
                              {:form-params
                               {:assertion assertion
                                :audience (verify-host host (get-hosts))}}))
                  true)]
    (when (= "okay" (:status verified))
      verified)))
