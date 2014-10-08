(ns credo.routes.base
  (:require [compojure.core :refer (defroutes ANY GET)]
            [compojure.route :as route]
            [noir.session :as session]
            [ring.util.response :as response]
            [liberator.core :refer [defresource]]
            [credo.api.hello :as hello]
            [credo.api.stormpath :as stormpath]))

(defn- test-session []
  (session/put! :testKey "test")
  (session/get :testKey))

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
  (GET "/" [] (response/redirect "index.html"))
  ;;(GET "/hello" [] "hello world!")
  (ANY "/session" [] (test-session))
  (ANY "/hello" {params :params} (hello-resource (:name params)))
  (ANY "/hello/:name" [name] (hello-resource name))
  (ANY "/login" [] (stormpath/login))
  ;;(ANY "/id" [] (response/redirect "/hello/world"))
  (ANY "/id" [request] (stormpath/id request))
  (route/resources "/")
  (route/files "/" {:root "resources/public"})
  (route/not-found "not found"))
