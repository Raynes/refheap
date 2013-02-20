(defproject refheap "1.4.0"
  :description "This is like, totally a pastebin, dude."
  :url "https://refheap.com"
  :dependencies [[org.clojure/clojure "1.5.0-RC16"]
                 [stencil "0.2.0"]
                 [compojure "1.1.3"]
                 [lib-noir "0.2.0"]
                 [com.novemberain/monger "1.4.0"]
                 [clj-config "0.2.0"]
                 [clj-http "0.4.0"]
                 [clavatar "0.2.0"]
                 [clj-time "0.3.3"]
                 [me.raynes/conch "0.4.0"]
                 [commons-codec/commons-codec "1.6"]
                 [org.pegdown/pegdown "1.2.1"]
                 [me.raynes/laser "0.1.26"]]
  :plugins [[lein-ring "0.8.2"]]
  :ring {:handler refheap.server/handler})

