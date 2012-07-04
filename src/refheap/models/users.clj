(ns refheap.models.users
  (:refer-clojure :exclude [sort find])  
  (:require [monger.collection :as mc]
            [monger.query :refer [with-collection find sort limit skip]]))

(defn get-user [user]
  (mc/find-one-as-map "users" {:username user}))

(defn get-user-by-id [id]
  ;; Monger takes care of coercing strings to object ids if necessary.
  (mc/find-map-by-id "users" id))

(defn user-pastes [user page & [others]]
  (with-collection "pastes"
    (find (merge {:user (str (:_id (get-user user)))} others))
    (sort {:date -1})
    (limit 10)
    ;; TODO: switch to Monger's pagination support. MK.
    (skip (* 10 (dec page)))))

(defn count-user-pastes [user & [others]]
  (mc/count "pastes" (merge {:user (str (:_id (get-user user)))} others)))
