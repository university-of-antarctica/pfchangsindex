(ns pfchangsindex.core)

(def avillePFC {:lat "35.48609758029149" :lon "-82.55266131970849"})
(def radius "10000")

(spit "/home/price/development/clojure/pfchangsindex/raw-out.txt"
      (pfchangsindex.geo/req-vector radius (:lat avillePFC) (:lon avillePFC)))

(def places-vec (pfchangsindex.geo/extract-places-vec
 (pfchangsindex.geo/req-vector radius (:lat avillePFC) (:lon avillePFC)) (vector)))

(defn places-vec-report [x]
  (let [vec_count (count x) set_count (count (into #{} x))]
    (println "vec-count: " vec_count " set_count: " set_count)))

(places-vec-report places-vec)

(pfchangsindex.db/createdb)

;;(for [place (req-vector)]
;;  (pfchangsindex.db/transact place))
(for [place (req-vector)]
  (clojure.pprint/pprint place))

(:price_level (get (req-vector) 1))

(pfchangsindex.db/query)
(pfchangsindex.db/del-db)

