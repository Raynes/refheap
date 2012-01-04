(ns refheap.models.users
  (:require [somnium.congomongo :as mongo]))

(defn get-user [user]
  (mongo/fetch-one
   :users
   :where {:username user}))

(defn get-user-by-id [id]
  (mongo/fetch-by-id :users (mongo/object-id id)))

(defn user-pastes [user page & [others]]
  (mongo/fetch
   :pastes
   :where (merge {:user (str (:_id (get-user user)))} others)
   :sort {:date -1}
   :limit 10
   :skip (* 10 (dec page))))

(defn count-user-pastes [user & [others]]
  (mongo/fetch-count
   :pastes
   :where (merge {:user (str (:_id (get-user user)))} others)))