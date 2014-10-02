(ns credo.core
  (:require [compojure.core :refer (defroutes ANY)]
            [compojure.route :as route]
            [compojure.handler :as handler]
            [liberator.core :refer [defresource]]
            [ring.util.response :as response]
            [ring.middleware.reload :as reload]
            [org.httpkit.server :as server]
            [org.httpkit.client :as client]
            [taoensso.timbre :as timbre]
            [datomic.api :as d])
  (:import (com.stormpath.sdk.client Client Clients)
           (com.stormpath.sdk.api ApiKey ApiKeys)
           ;;(com.stormpath.sdk.tenant)
           (com.stormpath.sdk.application Application Applications)
           (com.stormpath.sdk.account Account)
           (com.stormpath.sdk.authc UsernamePasswordRequest)
           (com.stormpath.sdk.idsite IdSiteUrlBuilder)))

(def path (str (System/getProperty "user.home") "/.stormpath/apiKey.properties"))

(def apikey (.build (.setFileLocation (ApiKeys/builder) path)))

(def client (.build (.setApiKey (Clients/builder) apikey)))

(defn- create-application [name] (let [application (.instantiate client Application)
                                application (.setName application name)
                                application (.createApplication 
                                             client 
                                             (.build 
                                              (.createDirectory 
                                               (Applications/newCreateRequestFor application))))]
                            application))

(def application (first (iterator-seq (.iterator (.getApplications client {"name" "credo"})))))

(def id-site (.newIdSiteUrlBuilder application))

(defn- configure-id-site [redirect-url]
  (.setCallbackUri id-site redirect-url))

(defn- get-id-site-url []
  (.build id-site))

(configure-id-site "http://localhost:8080/id")

(defn- create-account [username password firstname lastname email]
  (let [account (.instantiate client Account)]
    (.setGivenName account firstname)
    (.setSurname account lastname)
    (.setUsername account username)
    (.setEmail account email)
    (.setPassword account password)
    (.createAccount application account)))

;;(create-account)

(defn- authenticate-account [username password]
  (let [request (UsernamePasswordRequest. username password)
        result (.authenticateAccount application request)]
    (.getAccount  result)))

;;(.getUsername  (authenticate-account "shootz" "Welcome1"))

;;database
(def uri "datomic:mem://calx")

(def db (d/create-database uri))

(def conn (d/connect uri))

;;initialize schema
;;(d/transact conn  (read-string (slurp "./resources/schemas/graph.edn")))

;;functions
(defn hello [name]
  (timbre/info (str "saying hello to " name))
  (str "hello " name))

(defn handle-login-request []
  (response/redirect (get-id-site-url)))

;;liberator resources
(defresource hello-resource [name]
  ;;(timbre/info (str "in defresource with param " name))
  :available-media-types ["text/plain" "text/html"]
  :allowed-methods [:post :get]
  :post! (fn [_] (hello name))
  :post-redirect? true
  :location (str "/hello/" name)
  :handle-ok (fn [_] (hello name)))

;;compojure routes
(defroutes routes
  ;;(GET "/" [] "oh hai!")
  ;;(GET "/hello" [] "hello world!")
  (ANY "/hello" {params :params} (hello-resource (:name params)))
  (ANY "/hello/:name" [name] (hello-resource name))
  (ANY "/login" [] (handle-login-request))
  (ANY "/id" [] (response/redirect "http://localhost:8080/hello/world"))
  (route/files "/" {:root "resources/public"}))

;;app wrapper
(def app (-> (handler/site routes)
             reload/wrap-reload))

;;invoked on startup
(defn -main [& args]
  (timbre/info "starting application...")
  (let [port (Integer/parseInt
               (or (System/getenv "PORT") "8080"))]
    (server/run-server app {:port port :join? false}))
  (timbre/info "application started"))
