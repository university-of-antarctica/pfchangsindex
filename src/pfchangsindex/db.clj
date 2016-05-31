(ns pfchangsindex.db
  (:require [datomic.api :as d]))

(defn createdb []
  (def db-uri "datomic:free://localhost:4334/hello")
  (d/create-database db-uri)
  (def conn (d/connect db-uri)))

(defn transact [x]
  (def datom [:db/add (d/tempid :db.part/user)
              :db/place x])
  @(d/transact conn [datom]))


(defn query []
  (def db (d/db conn))
  (clojure.pprint/pprint (d/q '[:find ?e :where [?e :db/doc "hello world"]] db))
  (def dbquery (d/q '[:find ?e :where [?e :db/doc "hello world"]] db))
  (println  "query2" (d/q '[:find (count ?e) . :where [?e :db/doc]] db)))

(defn del-db []
  (if (d/delete-database db-uri)
    (println "db deleted")
    (println "db not deleted")))
