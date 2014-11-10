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
            [credo.api.database.init :as init :refer [conn]]
            [credo.api.spike.adherence :as spike]
            [datomic.api :as d]
            [net.cgrand.enlive-html :as html]))

;;database
;; (def uri "datomic:mem://credo")

;; (d/create-database uri)

;; (def conn (d/connect uri))

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
      [id (bigint id)
       entity (spike/full id)
       user (:user entity)
       metrics (:person/metrics (:user entity))
       invites (:invites entity)
       base-url-challenge "../challenges/"
       base-url-invite "../invites/"]
    
    (html/defsnippet invite-snippet "../resources/public/profile.html" 
      {[:div#inviteChallenge] [:div#inviteAcceptance]} 
      [invite]

      [:a#inviteChallengeLink] 
      (html/set-attr :href (str base-url-challenge (:db/id (:invite/challenge invite))))
      
      [:div#inviteChallenger] 
      (html/content (str (:person/email (:invite/challenger invite))))
      
      [:div#inviteMessage] 
      (html/content (str (:invite/message invite)))

      [:a#inviteAccept] 
      (html/set-attr :href (str base-url-invite (:db/id invite))))

    (html/deftemplate profile "../resources/public/profile.html" []
      [(html/attr= :name "__anti-forgery-token")] (html/set-attr :value af/*anti-forgery-token*)
      [:title] (html/content (str (:person/firstName user)))
      [:h1#pageTitle] (html/content (str (:person/firstName user) " " (:person/lastName user)))
      [:input#firstName] (html/set-attr 
                       :value (str (:person/firstName user)))
      [:input#lastName] (html/set-attr 
                       :value (str (:person/lastName user)))
      [:input#email] (html/set-attr 
                       :value (str (:person/email user)))
      [:input#height] (html/set-attr 
                       :value (str (:person.metrics/height metrics)))
      [:input#weight] (html/set-attr 
                       :value (str (:person.metrics/weight metrics)))
      [:div#invites] (html/content (map invite-snippet invites))))
  (reduce str (profile)))

(defn- set-profile [id height weight]
  (let [metrics-id (:db/id 
                    (:person/metrics 
                     (d/pull (d/db conn) [{:person/metrics [:db/id]}] (bigint id))))]
    (d/transact conn [{:db/id metrics-id 
                       :person.metrics/weight (Float/parseFloat weight)
                       :person.metrics/height (Float/parseFloat height)}]))
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


(defn- api-user [id]
  (let [id (bigint id)]
    (nr/json (spike/full id))))


;;compojure routes
(defroutes routes
  ;;index page
  (GET "/" [] (nr/redirect "index.html"))
  ;;(GET "/hello" [] "hello world!")

  ;;tests
  (ANY "/session" [] (test-session))

  ;;login/register
  (ANY "/login" [] (stormpath/login))
  
  ;;user - front end & tests
  (ANY "/id" request (stormpath/id request))
  (GET "/id/:id" [id] (get-profile id))
  (POST "/id/:id" [id height weight] (set-profile id height weight))
  (ANY "/id/:id/history/weight" [id] (get-weight-history id))

  ;;user - api
  (GET "/api/users/:id" [id] (api-user id) ) ;;TODO: implement get user api
  (POST "/api/users/:id" [id] () ) ;;TODO: implement set user api??
  

  ;;programs
  (GET "/api/programs/public" [] (nr/json(init/reify-entities(init/get-programs))))
  (GET "/api/programs/:programID" [programID] (nr/json
                                               (d/pull 
                                                (d/db conn) 
                                                '[*] [:program/id 
                                                      (java.util.UUID/fromString programID)])))
  ;;static resources
  (route/resources "/")
  (route/files "/" {:root "resources/public"})
  (route/not-found "not found"))


;; ;;liberator resources
;; (defresource hello-resource [name]
;;   ;;(timbre/info (str "in defresource with param " name))
;;   :available-media-types ["text/plain" "text/html"]
;;   :allowed-methods [:post :get]
;;   :post! (fn [_] (hello/hello name))
;;   :post-redirect? true
;;   :location (str "/hello/" name)
;;   :handle-ok (fn [_] (hello/hello name)))
