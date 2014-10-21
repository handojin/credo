(ns credo.routes.base
  (:require [compojure.core :refer (defroutes ANY GET POST)]
            [compojure.route :as route]
            [noir.session :as session]
            [ring.middleware.anti-forgery :as af]
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
      [(html/attr= :name "__anti-forgery-token")] (html/set-attr :value af/*anti-forgery-token*)
      [:title] (html/content (str (:person/firstName entity)))
      [:span#firstName] (html/content (str (:person/firstName entity)))
      [:span#lastName] (html/content (str (:person/lastName entity)))
      [:span#email] (html/content (str (:person/email entity)))
      [:input#height] (html/set-attr :value (str (:person/height entity)))
      [:input#weight] (html/set-attr :value (str (:person/weight entity)))))
  (reduce str (profile)))

(defn- set-profile [id height weight]
  (d/transact conn [{:db/id (bigint id) 
                     :person/weight (Float/parseFloat weight)
                     :person/height (Float/parseFloat height)}])
  (nr/redirect (str "http://localhost:8080/id/" id)))

(defn- get-weight-history [id]
  (str
   (d/q '[:find ?tx ?tx-time ?v 
          :in $ ?e ?a 
          :where [?e ?a ?v ?tx _] 
          [?tx :db/txInstant ?tx-time]] 
        (d/history (d/db conn)) 
        (bigint id) 
        :person/weight)))

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
  (GET "/id/:id" [id] (get-profile id))
  (POST "/id/:id" [id height weight] (set-profile id height weight))
  (ANY "/id/:id/history/weight" [id] (get-weight-history id))
  ;;static resources
  (route/resources "/")
  (route/files "/" {:root "resources/public"})
  (route/not-found "not found"))
