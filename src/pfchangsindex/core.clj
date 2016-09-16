(ns pfchangsindex.core
  (:require [pfchangsindex.geo :as pfgeo]))

;; Instructions for calculating a PF Chang's Index.
;; 1. Find a PF Chang's (google places API) 
;; 2. Pick a radius
;; 3. Get the rating of every restaurant within that radius
;; 4. Order the restaurants by rating.
;; 5. The PF Chang's index for that area is the PF Chang's element number.

;; For the first iteration we have chosen the asheville, NC PF Chang's
;; (I've had some awful fancy seeming meals there, presumably the exact
;; same one's anyone reading this may have had). The Radius is 1000 meters,
;; getting the rating of every restaurant within that radius is not easy.
;; google limits the number of restaurants it will return to you. Thus,
;; You must drill down with smaller and smaller searches within a given
;; radius to ensure you have an exhaustive list of the restaurants in
;; that area. Luckily, that's the only hard part so once that is done
;; you simply order the restaurants and figure out where PF Changs is
;; hiding.


(def avillePFC {:lat "35.48609758029149" :lon "-82.55266131970849"})
(def ask-google false) ;; T queries google, F uses text file.
(def radius "1000")

;; currently this spit is just here for testing purposes when I don't want to
;; waste google API calls.
;; (spit
;;  "/home/price/development/pfchangsindex/raw-out.txt"
;;  (pfchangsindex.geo/req-vector radius (:lat avillePFC) (:lon avillePFC)))

(def places-vec
  (if ask-google
    (pfchangsindex.geo/extract-places-vec
     (pfchangsindex.geo/req-vector radius (:lat avillePFC) (:lon avillePFC))
     (vector))
    (pfchangsindex.geo/extract-places-vec-stored)))


(defn places-vec-report [x]
  (let [vec_count (count x) set_count (count (into #{} x))]
    (println "vec-count: " vec_count " set_count: " set_count)))

(places-vec-report places-vec)

;;(pfchangsindex.db/createdb)

;;(for [place (req-vector)]
;;  (pfchangsindex.db/transact place))

;;(clojure.pprint/pprint places-vec)

;;(pfchangsindex.db/query)
;;(pfchangsindex.db/del-db)

