(ns credo.api.database.init
  (:require [datomic.api :as d]
            [noir.io :as io]))

;;database
(def uri "datomic:mem://credo")

(d/delete-database uri)
(d/create-database uri)

(def conn (d/connect uri))

;;initialize schema
(defn init-schema []
  (d/transact conn  (read-string (io/slurp-resource "/schemata/schema.edn"))))

(init-schema)

(defn init-users []
  (let [t       [{:db/id (d/tempid :db.part/user)
                  :person/account (java.net.URI. "http://test.credo.io")
                  :person/firstName "oliver"
                  :person/lastName "gorman"
                  :person/email "oli@test.credo.io"
                  :person.metrics/height 0.0
                  :person.metrics/weight 0.0}
                 {:db/id (d/tempid :db.part/user)
                  :person/account (java.net.URI. "http://test.credo.io")
                  :person/firstName "glenn"
                  :person/lastName "bates"
                  :person/email "glenn@test.credo.io"
                  :person.metrics/height 0.0
                  :person.metrics/weight 0.0}
                 {:db/id (d/tempid :db.part/user)
                  :person/account (java.net.URI. "http://test.credo.io")
                  :person/firstName "mike"
                  :person/lastName "nuteson"
                  :person/email "mike@test.credo.io"
                  :person.metrics/height 0.0
                  :person.metrics/weight 0.0}
                 {:db/id (d/tempid :db.part/user)
                  :person/account (java.net.URI. "http://test.credo.io")
                  :person/firstName "luis"
                  :person/lastName "andrade"
                  :person/email "luis@test.credo.io"
                  :person.metrics/height 0.0
                  :person.metrics/weight 0.0}
                 {:db/id (d/tempid :db.part/user)
                  :person/account (java.net.URI. "http://test.credo.io")
                  :person/firstName "christian"
                  :person/lastName "anschuetz"
                  :person/email "christian@test.credo.io"
                  :person.metrics/height 0.0
                  :person.metrics/weight 0.0}] 
        tx      @(d/transact conn t)]
    ;;(d/touch (d/entity db  (d/resolve-tempid db (:tempids tx) tID)))
    (str tx)))

(init-users)

(defn init-programs []
  (let [t       [{:db/id (d/tempid :db.part/user)
                  :program/id (d/squuid)
                  :program/name "science... it works"
                  :program/description "a science based approach to better health"
                  :program/parameters [{:program.parameter/id (d/squuid)
                                        :program.parameter/text 
                                        "I will get 8 hours of sleep every day"}
                                       {:program.parameter/id (d/squuid)
                                        :program.parameter/text 
                                        "I will do 15 minutes of interval sprints 3 times a week"}
                                       {:program.parameter/id (d/squuid)
                                        :program.parameter/text 
                                        "I will do stronglifts 5x5 workouts 3 times a week"}
                                       {:program.parameter/id (d/squuid)
                                        :program.parameter/text 
                                        "I will only consume my target calories"}
                                       {:program.parameter/id (d/squuid)
                                        :program.parameter/text 
                                        "I will drink 3l of water per day"}]
                  :program/type :program.type/public}
                 {:db/id (d/tempid :db.part/user)
                  :program/id (d/squuid)
                  :program/name "kmetz crossfit 722"
                  :program/description "the way of the cross"
                  :program/parameters [{:program.parameter/id (d/squuid)
                                        :program.parameter/text 
                                        "I will get 8 hours of sleep every day"}
                                       {:program.parameter/id (d/squuid)
                                        :program.parameter/text 
                                        "I will do 15 minutes of interval sprints 3 times a week"}
                                       {:program.parameter/id (d/squuid)
                                        :program.parameter/text 
                                        "I will do stronglifts 5x5 workouts 3 times a week"}]
                  :program/type :program.type/public}] 
        tx      @(d/transact conn t)]
    ;;(d/touch (d/entity db  (d/resolve-tempid db (:tempids tx) tID)))
    (str tx)))

(init-programs)

(defn init-challenge []
  (let [t       [{:db/id (d/tempid :db.part/user)
                  :challenge/id (d/squuid)
                  :program/name "science... it works"
                  :program/description "a science based approach to better health"
                  :program/parameters [{:program.parameter/id (d/squuid)
                                        :program.parameter/text 
                                        "I will get 8 hours of sleep every day"}
                                       {:program.parameter/id (d/squuid)
                                        :program.parameter/text 
                                        "I will do 15 minutes of interval sprints 3 times a week"}
                                       {:program.parameter/id (d/squuid)
                                        :program.parameter/text 
                                        "I will do stronglifts 5x5 workouts 3 times a week"}
                                       {:program.parameter/id (d/squuid)
                                        :program.parameter/text 
                                        "I will only consume my target calories"}
                                       {:program.parameter/id (d/squuid)
                                        :program.parameter/text 
                                        "I will drink 3l of water per day"}]
                  :program/type :program.type/public}
                 {:db/id (d/tempid :db.part/user)
                  :program/id (d/squuid)
                  :program/name "kmetz crossfit 722"
                  :program/description "the way of the cross"
                  :program/parameters [{:program.parameter/id (d/squuid)
                                        :program.parameter/text 
                                        "I will get 8 hours of sleep every day"}
                                       {:program.parameter/id (d/squuid)
                                        :program.parameter/text 
                                        "I will do 15 minutes of interval sprints 3 times a week"}
                                       {:program.parameter/id (d/squuid)
                                        :program.parameter/text 
                                        "I will do stronglifts 5x5 workouts 3 times a week"}]
                  :program/type :program.type/public}] 
        tx      @(d/transact conn t)]
    ;;(d/touch (d/entity db  (d/resolve-tempid db (:tempids tx) tID)))
    (str tx)))
