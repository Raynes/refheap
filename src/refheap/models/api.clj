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
  (let [old (users/get-user-by-id userid)
        new (gen-token)]
    (mongo/update!
     :users
     old
     (assoc old :token new))
    new))

(defn get-token
  "Get a user's API token. Generate one if it doesn't exist."
  [userid]
  (or (:token (users/get-user-by-id userid))
      (:token (new-token userid))))

(defn validate-user
  "Validate that a token exists and that the user has that token."
  [username token]
  (when (and username token)
    (if-let [user (mongo/fetch-one
                   :users
                   :where {:token token, :username (.toLowerCase username)})]
      (-> user
          (assoc :id (str (:_id user)))
          (dissoc :_id))
      "User or token not valid.")))

(defn process-paste
  "Select and rename keys to make pastes suitable for api consumption."
  [paste]
  (prn paste)
  (-> paste
      (assoc :contents (:raw-contents paste))
      (assoc :user (when-let [user (:user paste)]
                     (:username (users/get-user-by-id user))))
      (assoc :url (str "http://refheap.com/paste/" (:paste-id paste)))
      (dissoc :id :_id :raw-contents :summary)))

(defn string->bool [s]
  (case s
    "true" true
    "false" false
    false))