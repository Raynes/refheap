(ns refheap.models.api
  (:require [somnium.congomongo :as mongo]
            [refheap.models.users :as users])
  (:import java.util.UUID))

(defn gen-token
  "Create a new API token. This just generates a UUID."
  []
  (str (UUID/randomUUID)))

(defn new-token
  "Gives a user a new API token and disables the old one (by removing it)."
  [userid]
  (let [old (users/get-user-by-id userid)]
    (mongo/update!
     :users
     old
     (assoc old :token (gen-token)))))

(defn get-token
  "Get a user's API token. Generate one if it doesn't exist."
  [userid]
  (or (:token (users/get-user-by-id userid))
      (:token (new-token userid))))