(ns refheap.models.paste
  (:refer-clojure :exclude [sort find])
  (:require [noir.session :as session]
            [clojure.java.io :as io]
            [clojure.string :as string]
            [clj-time.core :as time]
            [clj-time.format :as format]
            [conch.core :as sh]
            [refheap.dates :refer [parse-string]]
            [refheap.messages :refer [error]]
            [monger.collection :as mc]
            [monger.query :refer [with-collection find sort limit skip]]
            [refheap.pygments :refer [lookup-lexer pygmentize]])
  (:import java.io.StringReader
           org.apache.commons.codec.digest.DigestUtils))

(def paste-id
  "The current highest paste-id."
  (atom
   (-> (with-collection "pastes"
         (find {})
         (sort {:id -1})
         (limit 1))
       first
       :id
       (or 0))))

(defn preview
  "Get the first 5 lines of a string."
  [s]
  (->> s StringReader. io/reader line-seq (take 5) (string/join "\n")))

(defn generate-id
  "Generate a hex string of a SHA1 hack of a random UUID.
   Return the first 25 characters."
  []
  (-> (java.util.UUID/randomUUID)
      str
      DigestUtils/shaHex
      (.substring 0 25)))

;; The reason there are three ids are because they all serve a different purpose.
;; paste-id is the id that is public-facing. If a paste is private, it is the same
;; as :random-id. If the paste is not private, it is the same as :id. :id is just
;; the number of the paste in the database. random-id is an id generated with
;; generate-id.
(defn paste-map [id random-id user language contents date private fork]
  (let [[name {:keys [short]}] (lookup-lexer language)
        private (boolean private)
        random-id (or random-id (generate-id))
        pygmentized (pygmentize short contents true)]
    (if-let [highlighted (:success pygmentized)]
      {:paste-id (if private random-id (str id))
       :id id
       :random-id random-id
       :user (:id user)
       :language name
       :raw-contents contents
       :summary (:success (pygmentize short (preview contents)))
       :private (boolean private)
       :date date
       :lines (let [lines (count (filter #{\newline} contents))]
                (if (= \newline (last contents))
                  lines
                  (inc lines)))
       :contents highlighted
       :fork fork}
      {:error (:error pygmentized)})))

(defn validate [contents]
  (cond
    (>= (count contents) 64000) {:error "That paste was too big. Has to be less than 64KB"}
    (not (re-seq #"\S" (str contents))) {:error "Your paste cannot be empty."}
    :else {:contents contents}))

(defn parse-date [date]
  (format/parse))

(defn paste
  "Create a new paste."
  [language contents private user & [fork]]
  (let [validated (validate contents)]
    (if-let [error (:error validated)]
      error
      (let [id (swap! paste-id inc)
            random-id (generate-id)
            paste (paste-map id
                    random-id
                    user
                    language
                    (:contents validated)
                    (format/unparse (format/formatters :date-time) (time/now))
                    private
                    fork)]
        (if-let [error (:error paste)]
          error
          (mc/insert-and-return "pastes" paste))))))

(defn get-paste
  "Get a paste."
  [id]
  (mc/find-one-as-map "pastes" {:paste-id id}))

(defn get-paste-by-id
  "Get a paste by its :id key (which is the same regardless of being public or private."
  [id]
  (mc/find-one-as-map "pastes" {:id id}))

(defn update-paste
  "Update an existing paste."
  [old language contents private user]
  (let [validated (validate contents)
        error (:error validated)]
    (cond
      error error
      (nil? user) "You must be logged in to edit pastes."
      (not= (:id user) (:user old)) "You can only edit your own pastes!"
      :else (let [{old-id :id random-id :random-id} old
                  paste (paste-map
                         old-id
                         random-id
                         user
                         language
                         (:contents validated)
                         (:date old)
                         private
                         (:fork old))]
              (if-let [error (:error paste)]
                error
                (mc/update "pastes" {:id old-id} paste :upsert false :multi false))
              paste))))

(defn delete-paste
  "Delete an existing paste."
  [id]
  (mc/remove "pastes" {:paste-id id}))

(defn get-pastes
  "Get public pastes."
  [page]
  ;; TODO: monger.query provides proper pagination support, I think it
  ;; makes sense to switch to that later. MK.
  (with-collection "pastes"
    (find {:private false})
    (sort {:date -1})
    (limit 20)
    (skip (* 20 (dec page)))))

(defn count-pastes
  "Count pastes."
  [& [private?]]
  (mc/count "pastes" (if-not (nil? private?)
                       {:private private?}
                       {})))

(defn count-pages [n per]
  (long (Math/ceil (/ n per))))

(defn proper-page [n]
  (if (<= n 0) 1 n))
