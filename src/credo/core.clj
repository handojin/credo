(ns credo.core
  (:require [org.httpkit.server :as server]
            [taoensso.timbre :as timbre]
            [credo.handler :refer [app]]))

;;invoked on startup
(defn -main [& args]
  (timbre/info "starting application...")
  (let [port (Integer/parseInt
               (or (System/getenv "PORT") "8080"))]
    (server/run-server app {:port port :join? false}))
  (timbre/info "application started"))

