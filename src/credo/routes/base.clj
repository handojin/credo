(ns credo.routes.base
  (:require [compojure.core :refer (defroutes ANY GET)]
            [compojure.route :as route]
            [noir.session :as session]
            ;;[ring.util.response :as response]
            [noir.response :as nr]
            [clojure.data.json :as json]
            [liberator.core :refer [defresource]]
            [credo.api.hello :as hello]
            [credo.api.stormpath :as stormpath]
            [datomic.api :as d]
            [net.cgrand.enlive-html :as html]))

;;database
(def uri "datomic:mem://credo")

(d/create-database uri)

(def conn (d/connect uri))

(defn- test-session []
  ;;(session/put! :testKey "test")
  (str (session/get :entity)))

(defn- datomic-content []
  (json/write-str (d/q '[:find ?e :where [?e :person/firstName]] 
                       (d/db conn))))

(defn- get-entity []
  (str  (d/touch (d/entity (d/db conn) (BigInteger. "17592186045420")))))

(defn- get-profile [id]
  (let 
      [idx (bigint id)
       entity (d/touch (d/entity (d/db conn) idx))]
    (html/deftemplate profile "../resources/public/profile.html" []
      [:title] (html/content (str (:person/firstName entity)))
      [:span#firstName] (html/content (str (:person/firstName entity)))
      [:span#lastName] (html/content (str (:person/lastName entity)))
      [:span#email] (html/content (str (:person/email entity)))
      [:input#height] (html/set-attr :value "6'00''")
      [:input#weight] (html/set-attr :value "202.5 lb")))
  (reduce str (profile)))

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
  ;;index page
  (GET "/" [] (nr/redirect "index.html"))
  ;;(GET "/hello" [] "hello world!")

  ;;tests
  (ANY "/session" [] (test-session))
  (ANY "/datomic-test" [] (nr/json (datomic-content)))
  (ANY "/entity-test" [] (get-entity))
  (ANY "/datomic" [] (stormpath/test-datomic))
  (ANY "/hello" {params :params} (hello-resource (:name params)))
  (ANY "/hello/:name" [name] (hello-resource name))

  ;;login/register
  (ANY "/login" [] (stormpath/login))
  (ANY "/id" request (stormpath/id request))
  ;;TODO - profile page
  (ANY "/id/:id" [id] (get-profile id))

  ;;static resources
  (route/resources "/")
  (route/files "/" {:root "resources/public"})
  (route/not-found "not found"))
