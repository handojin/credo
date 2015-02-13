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




(defn- get-challenge [cID uID]
  (let
      [cID (bigint cID)
       uID (bigint uID)
       challenge (spike/challenge cID)
       adherence (spike/adherence uID)]
    
    (html/defsnippet parameters-exceptions-snippet "../resources/public/challenge_template.html" 
      {[:span#parameters] [:p#exception]}
      [parameter-exception]
      
      [:p#parameter]
      (html/content (str 
                     (:program.parameter/displayText 
                      (:challenge.exception/parameter parameter-exception))))

      [:p#exception]
      (html/content (str 
                     (:challenge.exception/quantity parameter-exception))))

    (html/defsnippet adherence-snippet "../resources/public/challenge_template.html" 
      {[:p#question] [:input#responseNo]}
      [adherence-item]
      
      [:p#question] 
      (html/content (str 
                     (:program.parameter/questionText 
                      (:adherence.item/parameter adherence-item))))
      
      [:input#responseYes]
      (html/set-attr :name (str 
                            (:db/id adherence-item)))
      [:input#responseNo]
      (html/set-attr :name (str 
                            (:db/id adherence-item))))
     

    (html/deftemplate challenge-template "../resources/public/challenge_template.html" []
      [(html/attr= :name "__anti-forgery-token")] (html/set-attr :value af/*anti-forgery-token*)
      
      [:title] 
      (html/content (:program/name (:challenge/program challenge)))
      
      [:h1#pageTitle] 
      (html/content (:program/name (:challenge/program challenge)))
      
      [:span#challengeName]
      (html/content (:program/name (:challenge/program challenge)))
      
      [:span#challengeDesc] 
      (html/content (:program/description (:challenge/program challenge)))
   
      [:div#challengeSpec] 
      (html/content (map parameters-exceptions-snippet (:challenge/exceptions challenge)))
      
      [:div#challengeAdherence] 
      (html/content (map 
                     adherence-snippet 
                     (:adherence.header/items (first (:person/adherence adherence))))))

     (reduce str (challenge-template))))



(defn- set-profile [id height weight]
  (let [metrics-id (:db/id 
                    (:person/metrics 
                     (d/pull (d/db conn) [{:person/metrics [:db/id]}] (bigint id))))]
    (d/transact conn [{:db/id metrics-id 
                       :person.metrics/weight (Float/parseFloat weight)
                       :person.metrics/height (Float/parseFloat height)}]))
  (nr/redirect (str "http://localhost:8080/id/" id)))



(defn- get-weight-history [id]
  (let [id (bigint id)
        id (:db/id 
            (:person/metrics 
             (d/pull (d/db conn) '[{:person/metrics [:db/id]}] id)))]
    (str
     (d/q '[:find ?tx ?tx-time ?v 
            :in $ ?e ?a 
            :where [?e ?a ?v ?tx _] 
            [?tx :db/txInstant ?tx-time]] 
          (d/history (d/db conn)) 
          (bigint id) 
          :person.metrics/weight))))

(defn- get-adherence-entities [uID cID]
  (let [uID (bigint uID)
        cID (bigint cID)
        aID (d/q 
             '[:find ?v :in $ ?uID ?cID 
               :where 
               [?uID :person/adherence ?aID] 
               [?aID :adherence.header/challenge ?cID] 
               [_ :adherence.header/items ?v]] (d/db conn) uID cID)] (sort-by first aID)))
    
(defn- get-adherence-item-entity-history [eID]
  (let [eID (first eID)

        adherence-txs (d/q '[:find ?tx ?tx-time
                             :in $ ?e
                             :where 
                             [?e _ _ ?tx _]
                             [?tx :db/txInstant ?tx-time]]
                           (d/history (d/db conn)) eID) 
        
        adherence-history (map 
                           #(conj (hash-map :tx-time (second %)) 
                                  (d/touch 
                                   (d/entity 
                                    (d/as-of (d/db conn) (first %)) eID))) 
                           (reverse (sort-by first adherence-txs)))]
    
    (hash-map 
     :entity eID 
     :history (map 
               #(hash-map 
                 :timestamp (first %) 
                 :value (first (second %)))  
               (group-by :adherence.item/date adherence-history)))))



(defn- get-adherence-history [uID cID]
  (map #(get-adherence-item-entity-history %) (get-adherence-entities uID cID)))

(defn- api-user [id]
  (let [id (bigint id)]
    (nr/json (spike/full id))))


(defn- set-adherence [cID uID adherence-date params]
  (let [adherence-params (map bigint  
                           (filter seq 
                                   (map #(re-find  #"[0-9]*" %) (map name (keys params)))))
        t (mapv #(hash-map 
                 :db/id % 
                 :adherence.item/date (clojure.instant/read-instant-date adherence-date)
                 :adherence.item/value (if (= "true" (get params (str %))) true false)) adherence-params)
        
        tx @(d/transact conn t)] (nr/redirect (str "/id/" uID "/challenge/" cID))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
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
  ;;user challenge
  (GET "/id/:uID/challenge/:cID" [cID uID] (get-challenge cID uID))
  ;;challenge adherence
  (POST "/id/:uID/challenge/:cID" 
        [cID uID adherenceDate & params] (set-adherence cID uID adherenceDate params))

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
