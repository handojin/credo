(ns credo.routes.base
  (:require [compojure.core :refer (defroutes ANY)]
            [compojure.route :as route]
            [ring.util.response :as response]
            [liberator.core :refer [defresource]]
            [credo.api.hello :as hello]
            [credo.api.stormpath :as stormpath]))

;;liberator resources
(defresource hello-resource [name]
  ;;(timbre/info (str "in defresource with param " name))
  :available-media-types ["text/plain" "text/html"]
  :allowed-methods [:post :get]
  :post! (fn [_] (hello/hello name))
  :post-redirect? true
  :location (str "/hello/" name)
  :handle-ok (fn [_] (hello/hello name)))

;;compojure routes
(defroutes routes
  ;;(GET "/" [] "oh hai!")
  ;;(GET "/hello" [] "hello world!")
  (ANY "/hello" {params :params} (hello-resource (:name params)))
  (ANY "/hello/:name" [name] (hello-resource name))
  (ANY "/login" [] (stormpath/login))
  (ANY "/id" [] (response/redirect "http://localhost:8080/hello/world"))
  (route/files "/" {:root "resources/public"}))
