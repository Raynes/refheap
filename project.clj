(defproject refheap "1.1.0"
  :description "This is like, totally a pastebin, dude."
  :url "https://refheap.com"
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [stencil "0.2.0"]
                 [noir "1.3.0-beta10"]
                 [lib-noir "0.2.0-alpha1"]
                 [congomongo "0.1.9"]
                 [clj-config "0.2.0"]
                 [clj-http "0.4.0"]
                 [clavatar "0.2.0"]
                 [clj-time "0.3.3"]
                 [amalloy/mongo-session "0.0.2" :exclusions [ring/ring-core]]
                 [conch "0.3.0"]
                 [org.bovinegenius/exploding-fish "0.2.0"]]
  :main refheap.server)

