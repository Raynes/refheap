(defproject refheap "1.4.0"
  :description "This is like, totally a pastebin, dude."
  :url "https://refheap.com"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [stencil "0.3.2"]
                 [compojure "1.1.5"]
                 [lib-noir "0.6.4"]
                 [com.novemberain/monger "1.5.0"]
                 [clj-config "0.2.0"]
                 [clj-http "0.7.3" :exclusions [org.jsoup/jsoup]]
                 [clavatar "0.2.1"]
                 [clj-time "0.5.1"]
                 [me.raynes/conch "0.5.1"]
                 [commons-codec/commons-codec "1.8"]
                 [me.raynes/cegdown "0.1.0"]
                 [me.raynes/laser "1.1.1"]]
  :plugins [[lein-ring "0.8.5"]]
  :ring {:handler refheap.server/handler})

