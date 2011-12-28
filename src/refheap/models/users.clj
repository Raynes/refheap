(ns refheap.models.users
  (:require [somnium.congomongo :as mongo]))

(defn get-user [user]
  (mongo/fetch-one
   :users
   :where {:username user}))

(defn user-pastes [user page]
  (mongo/fetch
   :pastes
   :where {:user user}
   :limit 10
   :skip (* 10 (dec page))))

(defn count-user-pastes [user]
  (mongo/fetch-count
   :pastes
   :where {:user user}))

(defn count-pages [n]
  (Math/ceil (/ n 10)))