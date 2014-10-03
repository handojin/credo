(ns credo.core
  (:require [org.httpkit.server :as server]
            [taoensso.timbre :as timbre]
            [datomic.api :as d]
            [credo.handler :refer [app]]))

;;database
(def uri "datomic:mem://calx")

(def db (d/create-database uri))

(def conn (d/connect uri))

;;initialize schema
;;(d/transact conn  (read-string (slurp "./resources/schemas/graph.edn")))

;;invoked on startup
(defn -main [& args]
  (timbre/info "starting application...")
  (let [port (Integer/parseInt
               (or (System/getenv "PORT") "8080"))]
    (server/run-server app {:port port :join? false}))
  (timbre/info "application started"))

