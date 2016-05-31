(ns pfchangsindex.core)

(def avillePFC {:lat "35.48609758029149" :lon "-82.55266131970849"})
(def radius "50000")

(spit "/home/price/development/clojure/pfchangsindex/raw-out.txt" (pfchangsindex.geo/req-vector radius (:lat avillePFC) (:lon avillePFC)))

(pfchangsindex.db/createdb)

;;(for [place (req-vector)]
;;  (pfchangsindex.db/transact place))
(for [place (req-vector)]
  (clojure.pprint/pprint place))

(:price_level (get (req-vector) 1))

(pfchangsindex.db/query)
(pfchangsindex.db/del-db)

