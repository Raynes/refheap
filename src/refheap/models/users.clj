(ns refheap.models.users
  (:require [somnium.congomongo :as mongo]))

(defn get-user [user]
  (mongo/fetch-one
   :users
   :where {:username user}))

(defn user-pastes [user page & [others]]
  (mongo/fetch
   :pastes
   :where (merge {:user user} others)
   :sort {:date -1}
   :limit 10
   :skip (* 10 (dec page))))

(defn count-user-pastes [user & [others]]
  (mongo/fetch-count
   :pastes
   :where (merge {:user user} others)))