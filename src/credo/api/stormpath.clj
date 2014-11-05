(ns credo.api.stormpath
  (:require [noir.response :as response]
            [noir.session :as session]
            [noir.io :as io]
            [datomic.api :as d]
            [credo.api.database.init :as init :refer [conn]])
  (:import (com.stormpath.sdk.client Client Clients)
           (com.stormpath.sdk.api ApiKey ApiKeys)
           (com.stormpath.sdk.application Application Applications)
           (com.stormpath.sdk.account Account)
           (com.stormpath.sdk.authc UsernamePasswordRequest)
           (com.stormpath.sdk.http HttpMethod HttpRequest HttpRequests)
           (com.stormpath.sdk.idsite IdSiteUrlBuilder IdSiteCallbackHandler)))

;;(def path (str (System/getProperty "user.home") "/.stormpath/apiKey.properties"))

(if  (nil? (System/getProperty "PARAM1")) (System/setProperty "PARAM1" (System/getenv "PARAM1")))
(if  (nil? (System/getProperty "PARAM2")) (System/setProperty "PARAM2" (System/getenv "PARAM2")))

(def apikey (-> 
             (ApiKeys/builder) 
             (.setId (System/getProperty "PARAM1")) 
             (.setSecret (System/getProperty"PARAM2")) 
             (.build)))

;;(def apikey (.build (.setFileLocation (ApiKeys/builder) path)))

(def client (.build (.setApiKey (Clients/builder) apikey)))

(def application (first (iterator-seq (.iterator (.getApplications client {"name" "credo"})))))

(def id-site (.newIdSiteUrlBuilder application))

(defn set-id-site-callback-uri [redirect-url]
  (.setCallbackUri id-site redirect-url))

(defn get-id-site-url []
  (.build id-site))

(set-id-site-callback-uri "http://localhost:8080/id")

(defn create-application [name] 
  (let [application (.instantiate client Application)
        application (.setName application name)
        application (.createApplication 
                     client 
                     (.build 
                      (.createDirectory  (Applications/newCreateRequestFor application))))]
    application))

(defn create-account [username password firstname lastname email]
  (let [account (.instantiate client Account)]
    (.setGivenName account firstname)
    (.setSurname account lastname)
    (.setUsername account username)
    (.setEmail account email)
    (.setPassword account password)
    (.createAccount application account)))

(defn authenticate-account [username password]
  (let [request (UsernamePasswordRequest. username password)
        result (.authenticateAccount application request)]
    (.getAccount  result)))

;;TODO - refactor from here

(defn login []
  (response/redirect (get-id-site-url)))

;; ;;database
;; (def uri "datomic:mem://credo")

;; (d/create-database uri)

;; (def conn (d/connect uri))

;;initialize schema
;;(d/transact conn  (read-string (io/slurp-resource "/schemata/schema.edn")))

(defn- new-profile [result]
  (let [account (.getAccount result)
        ;;custom  (.getCustomData account)
        ;;conn    (d/connect "datomic:mem://credo")
        db      (d/db conn)
        tID     (d/tempid :db.part/user)
        tx      [{:db/id tID 
                  :person/account (.getHref account)
                  :person/firstName (.getGivenName account)
                  :person/lastName (.getSurname account)
                  :person/email (.getEmail account)
                  :person/metrics {:person.metrics/height 0.0
                   :person.metrics/weight 0.0}}]
        tx      @(d/transact conn tx)
        eID     (d/resolve-tempid (d/db conn) (:tempids tx) tID)
        ]
    ;;(.put custom "entityID" eID)
    ;;(.save account)
    (session/put! :entity eID)
    (response/redirect (str "http://localhost:8080/id/" eID))))

(defn id [request] 
  (let [request (->  
                 (HttpRequests/method HttpMethod/GET)
                 (.headers (:headers request))
                 (.queryParameters (:query-string request))
                 (.build))
        result  (.getAccountResult (.newIdSiteCallbackHandler application request))
        account (.getAccount result)]

    ;;howto - manipulate session
    ;;(session/put! :testKey (.getEmail account))
    (if (.isNewAccount result) (new-profile result))))


;; (d/q '[:find ?e :where [?e :person/firstName ?a]] (d/db conn))
;; ;; (d/touch (d/entity (d/db (d/connect "datomic:mem://credo")) 17592186045453 ))
