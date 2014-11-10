(ns credo.api.database.init
  (:require [datomic.api :as d]
            [noir.io :as io]))

;;database
(def uri "datomic:free://localhost:4334/credo")

;(d/delete-database uri)
(d/create-database uri)


(def conn (d/connect uri))

;;initialize schema
(defn init-schema []
  (d/transact conn  (read-string (io/slurp-resource "/schemata/schema.edn"))))

(init-schema)

(defn init-users []
  (let [t       [{:db/id (d/tempid :db.part/user)
                  :person/account "http://oli.test.credo.io"
                  :person/firstName "oliver"
                  :person/lastName "gorman"
                  :person/email "oli@test.credo.io"
                  :person/metrics {:person.metrics/id (d/squuid)
                                   :person.metrics/height 0.0
                                   :person.metrics/weight 0.0}}
                 
                 {:db/id (d/tempid :db.part/user)
                  :person/account "http://glenn.test.credo.io"
                  :person/firstName "glenn"
                  :person/lastName "bates"
                  :person/email "glenn@test.credo.io"
                  :person/metrics {:person.metrics/id (d/squuid)
                                   :person.metrics/height 0.0
                                   :person.metrics/weight 0.0}}
                 
                 {:db/id (d/tempid :db.part/user)
                  :person/account "http://mike.test.credo.io"
                  :person/firstName "mike"
                  :person/lastName "nuteson"
                  :person/email "mike@test.credo.io"
                  :person/metrics {:person.metrics/id (d/squuid)
                                   :person.metrics/height 0.0
                                   :person.metrics/weight 0.0}}
                 
                 {:db/id (d/tempid :db.part/user)
                  :person/account "http://luis.test.credo.io"
                  :person/firstName "luis"
                  :person/lastName "andrade"
                  :person/email "luis@test.credo.io"
                  :person/metrics {:person.metrics/id (d/squuid)
                                   :person.metrics/height 0.0
                                   :person.metrics/weight 0.0}}
                 
                 {:db/id (d/tempid :db.part/user)
                  :person/account "http://christian.test.credo.io"
                  :person/firstName "christian"
                  :person/lastName "anschuetz"
                  :person/email "christian@test.credo.io"
                  :person/metrics {:person.metrics/id (d/squuid)
                                   :person.metrics/height 0.0
                                   :person.metrics/weight 0.0}}]
 
        tx      @(d/transact conn t)]
    ;;(d/touch (d/entity db  (d/resolve-tempid db (:tempids tx) tID)))
    (str tx)))

(init-users)

(defn init-programs []
  (let [t       [{:db/id (d/tempid :db.part/user)

                  :program/id (d/squuid)

                  :program/name "science... it works"

                  :program/description "a science based approach to better health"

                  :program/parameters
                  [{:program.parameter/id (d/squuid)
                    :program.parameter/displayText "I will get 8 hours of sleep every day"
                    :program.parameter/questionText "Did you get 8 hours sleep?"
                    :program.parameter/quantity 8
                    :program.parameter/period 1}
                   
                   {:program.parameter/id (d/squuid)
                    :program.parameter/displayText "I will do 15 minutes of interval sprints 3 times a week"
                    :program.parameter/questionText "Did you do your cardio workout?"
                    :program.parameter/quantity 3
                    :program.parameter/period 7}

                   {:program.parameter/id (d/squuid)
                    :program.parameter/displayText "I will do stronglifts 5x5 workouts 3 times a week"
                    :program.parameter/questionText "Did you do your strength training workout?"
                    :program.parameter/quantity 3
                    :program.parameter/period 7}

                   {:program.parameter/id (d/squuid)
                    :program.parameter/displayText "I will only consume my target calories"
                    :program.parameter/questionText "Did you stay within your calorie budget?"
                    :program.parameter/quantity 1
                    :program.parameter/period 1}

                   {:program.parameter/id (d/squuid)
                    :program.parameter/displayText "I will drink 3l of water per day"
                    :program.parameter/questionText "Did you drink your water"
                    :program.parameter/quantity 8
                    :program.parameter/period 1}]

                  :program/type :program.type/public}

                 {:db/id (d/tempid :db.part/user)

                  :program/id (d/squuid)

                  :program/name "kmetz crossfit 722"

                  :program/description "the way of the cross"

                  :program/parameters
                  [{:program.parameter/id (d/squuid)
                    :program.parameter/displayText "I will do 15 minutes of interval sprints 3 times a week"
                    :program.parameter/questionText "Did you do your cardio workout?"
                    :program.parameter/quantity 3
                    :program.parameter/period 7}

                   {:program.parameter/id (d/squuid)
                    :program.parameter/displayText "I will do stronglifts 5x5 workouts 3 times a week"
                    :program.parameter/questionText "Did you do your strength training workout?"
                    :program.parameter/quantity 3
                    :program.parameter/period 7}

                   {:program.parameter/id (d/squuid)
                    :program.parameter/displayText "I will only consume my target calories"
                    :program.parameter/questionText "Did you stay within your calorie budget?"
                    :program.parameter/quantity 1
                    :program.parameter/period 1}]  

                   :program/type :program.type/public}] 
        
                 tx      @(d/transact conn t)]
    ;;(d/touch (d/entity db  (d/resolve-tempid db (:tempids tx) tID)))
    (str tx)))

(init-programs)

(defn init-challenge []
  (let [t       [{:db/id (d/tempid :db.part/user)
                  :challenge/id (d/squuid)
                  :challenge/program 17592186045442
                  :challenge/startDate (clojure.instant/read-instant-date "2014-10-31")
                  :challenge/endDate (clojure.instant/read-instant-date "2014-12-31")

                  :challenge/exceptions 
                  [{:challenge.exception/id (d/squuid)
                    :challenge.exception/parameter 17592186045443 
                    :challenge.exception/quantity 4}

                   {:challenge.exception/id (d/squuid)
                    :challenge.exception/parameter 17592186045444 
                    :challenge.exception/quantity 3}

                   {:challenge.exception/id (d/squuid)
                    :challenge.exception/parameter 17592186045445
                    :challenge.exception/quantity  1}]}]
        
        tx      @(d/transact conn t)]
    ;;(d/touch (d/entity db  (d/resolve-tempid db (:tempids tx) tID)))
    (str tx)))

(init-challenge)

(defn issue-invite [challenger challengee challenge message]
  (let [t       [{:db/id (d/tempid :db.part/user)
                  :invite/id (d/squuid)
                  :invite/challenger challenger
                  :invite/challengee challengee
                  :invite/challenge challenge
                  :invite/message message
                  :invite/status :invite.status/issued}] 
        tx      @(d/transact conn t)]
    ;;(d/touch (d/entity db  (d/resolve-tempid db (:tempids tx) tID)))
    (str tx)))

(issue-invite 17592186045427 17592186045425 175921860454 "try some science in your diet")
;; (issue-invite 17592186045426 17592186045427 17592186045442 "you won't make it")

(defn accept-invite [invite]
  (let 
      [t [{:db/id invite :invite/status :invite.status/accepted}]
       tx @(d/transact conn t)
       
       challenge-ids (d/pull (d/db conn) '[:invite/challengee 
                                           :invite/challenger 
                                           :invite/challenge] invite)

       challenge-exceptions (:challenge/exceptions (d/pull 
                                                    (d/db conn) 
                                                    '[{:challenge/exceptions 
                                                       [:db/id :challenge.exception/parameter]}] (:db/id (:invite/challenge challenge-ids))))
       t [(conj  (:invite/challengee challenge-ids)
                 {:person/adherence {:adherence.header/id (d/squuid)
                                     :adherence.header/challenge (:invite/challenge challenge-ids)
                                     :adherence.header/date (java.util.Date.)
                                     :adherence.header/items (mapv #(hash-map :db/id (d/tempid :db.part/user) 
                                                                              :adherence.item/id (d/squuid) 
                                                                              :adherence.item/parameter (:challenge.exception/parameter %) 
                                                                              :adherence.item/value false) challenge-exceptions)}}
                 )]
       
       tx @(d/transact conn t)
       
       

       ;;program-id (d/pull (d/db conn) '[:challenge/program])

       ;;params 
       ]
    t))

(defn decline-invite [invite]
  (let [t [{:db/id invite :invite/status :invite.status/declined}]
        tx @(d/transact conn t)]
    (str tx)))

;;(accept-invite 17592186045452)

;; (decline-invite 17592186045449)

(defn get-users []
  (d/q '[:find ?e :where [?e :person/email]] (d/db conn)))

(defn get-programs []
  (d/q '[:find ?e :where [?e :program/id]] (d/db conn)))

(defn get-program-parameters []
  (d/q '[:find ?e :where [?e :program.parameter/id]] (d/db conn)))

(defn get-challenges []
  (d/q '[:find ?e :where [?e :challenge/id]] (d/db conn)))

(defn get-challenge-exceptions []
  (d/q '[:find ?e :where [?e :challenge.exception/id]] (d/db conn)))

(defn get-invites []
  (d/q '[:find ?e :where [?e :invite/id]] (d/db conn)))

(defn reify-entities [ids]
  (map #(d/touch (d/entity (d/db conn) (first  %))) ids))



