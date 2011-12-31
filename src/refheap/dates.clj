(ns refheap.dates
  (:require [clj-time.core :as time]
            [clj-time.format :as format]))

(def months
  {1 "January"
   2 "February"
   3 "March"
   4 "April"
   5 "May"
   6 "June"
   7 "July"
   8 "August"
   9 "September"
   10 "October"
   11 "November"
   12 "December"})

(defn parse-string [date]
  (format/parse (format/formatters :date-time) date))

(defn date-string [date]
  (let [parsed (parse-string date)]
    (str (months (time/month parsed)) " "
         (time/day parsed) ", "
         (time/year parsed))))