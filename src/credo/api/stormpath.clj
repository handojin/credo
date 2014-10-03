(ns credo.api.stormpath
  (:require [ring.util.response :as response])
  (:import (com.stormpath.sdk.client Client Clients)
           (com.stormpath.sdk.api ApiKey ApiKeys)
           (com.stormpath.sdk.application Application Applications)
           (com.stormpath.sdk.account Account)
           (com.stormpath.sdk.authc UsernamePasswordRequest)
           (com.stormpath.sdk.idsite IdSiteUrlBuilder)))

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

(defn login []
  (response/redirect (get-id-site-url)))

(set-id-site-callback-uri "http://localhost:8080/id")
