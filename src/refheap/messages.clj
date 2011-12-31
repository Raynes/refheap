(ns refheap.messages
  (:require [noir.session :as session]))

(defn error [msg]
  (session/flash-put! :error msg)
  nil)