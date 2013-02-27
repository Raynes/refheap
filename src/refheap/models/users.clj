(ns refheap.models.users
  (:refer-clojure :exclude [sort find])  
  (:require [monger.collection :as mc]
            [monger.query :refer [with-collection find sort paginate]])
  (:import org.bson.types.ObjectId))

(defn get-user [user]
  (mc/find-one-as-map "users" {:username user}))

(defn get-user-by-id [id]
  (mc/find-map-by-id "users" (ObjectId. id)))

(defn user-pastes [user page & [others]]
  (with-collection "pastes"
    (find (merge {:user (str (:_id (get-user user)))} others))
    (sort {:date -1})
    (paginate :page page :per-page 10)))

(defn count-user-pastes [user & [others]]
  (mc/count "pastes" (merge {:user (str (:_id (get-user user)))} others)))
