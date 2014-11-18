(ns credo.handler
  (:require [noir.util.middleware :as nm]
            [taoensso.timbre :as timbre]
            [taoensso.timbre.appenders.rotor :as rotor]
            [credo.routes.base :as base]
            [ring.middleware.reload :as reload]))

(defn init []
  (timbre/set-config! [:appenders :rotor]
                      {:min-level :info
                       :enabled? true
                       :async? false
                       :max-message-per-msecs nil
                       :fn rotor/appender-fn})
  (timbre/set-config! [:shared-appender-config :rotor]
                      {:path "credo.log"
                       :max-size (* 512 1024) 
                       :backlog 10})

  (timbre/info "credo started"))

(defn destroy []
  (timbre/info "credo shutdown"))

(def app (->
          (nm/app-handler [base/routes])
          (reload/wrap-reload)
          ))
