(defproject refheap "1.4.0"
  :description "This is like, totally a pastebin, dude."
  :url "https://refheap.com"
  :dependencies [[org.clojure/clojure "1.5.0-RC16"]
                 [stencil "0.2.0"]
                 [compojure "1.1.5"]
                 [lib-noir "0.5.6"]
                 [com.novemberain/monger "1.5.0-beta1"]
                 [clj-config "0.2.0"]
                 [clj-http "0.6.4" :exclusions [org.jsoup/jsoup]]
                 [clavatar "0.2.1"]
                 [clj-time "0.4.4"]
                 [me.raynes/conch "0.5.1"]
                 [commons-codec/commons-codec "1.6"]
                 [me.raynes/cegdown "0.1.0"]
                 [me.raynes/laser "1.1.1"]]
  :plugins [[lein-ring "0.8.2"]]
  :ring {:handler refheap.server/handler})

