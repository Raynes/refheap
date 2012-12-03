(defproject refheap "1.3.0"
  :description "This is like, totally a pastebin, dude."
  :url "https://refheap.com"
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [stencil "0.2.0"]
                 [compojure "1.1.3"]
                 [lib-noir "0.2.0"]
                 [com.novemberain/monger "1.4.0"]
                 [clj-config "0.2.0"]
                 [clj-http "0.4.0"]
                 [clavatar "0.2.0"]
                 [clj-time "0.3.3"]
                 [conch "0.3.0"]
                 [commons-codec/commons-codec "1.6"]]
  :plugins [[lein-ring "0.7.1"]]
  :ring {:handler refheap.server/handler
         :auto-reload? false})

