(ns refheap.config
  (:require [clj-config.core :as cfg]))

(def config
  "Some external configuration."
  (cfg/safely cfg/read-config "config.clj"))