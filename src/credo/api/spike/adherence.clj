(ns credo.api.spike.adherence
  (:require [datomic.api :as d]
            [credo.api.database.init :as init :refer [conn]]))

;;user api

;;given a user id, return all info about user, challenges, invites etc.

(defn user [id]
  (d/pull (d/db conn) '[* {:person/adherence
                           [{:adherence.header/challenge  [:challenge/id :challenge/startDate]}]}] id))

(defn invites [id]
  (d/pull-many (d/db conn) '[*] 
               (flatten 
                (seq  
                 (d/q '[:find ?e :in $ ?id :where [?e :invite/challengee ?id]] (d/db conn) id)))))



(defn full [id]
  {:user (user id) :invites (invites id)})
