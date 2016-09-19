(ns pfchangsindex.core)

;; GLOBAL TODO
;; 1. Write a new ns (geog.clj) that uses the api-query functionality to create
;;    a proper set of all restaurants around the avillePFC. As the program stands
;;    we are limited by the fact that radii that are too large mean that more than
;;    60 restaurants (the amt. google limits you to) are within that area. To make
;;    a legit PFC index we need to get the ratings of all the restaurants in a pre-
;;    defined area (I'm arbitrarily leaning towards 3 miles) which means that geog.clj
;;    will essentially be a wrapper around api-query.clj that extends the fcnality
;;    of the google places API to getting a complete set of restaurants within a given
;;    radius, not just "up to 60 results".
;; 2. use yelp too: https://www.yelp.com/developers/documentation/v2/overview

;; doc link for places api!!!
;; https://developers.google.com/places/web-service/intro

(def avillePFC {:lat "35.48609758029149" :lon "-82.55266131970849"})
(def ask-google true) ;; T queries google, F uses text file.
;; 3 miles is 4828 , currently 1000 always contains the aville PFC
(def radius "1000")

;; currently this spit is just here for testing purposes when I don't want to
;; waste google API calls. With ask-google set to false, when you define the
;; places-vec variable it calls extract-places-vec-stored which just slurps
;; the raw-out.txt file and puts it into a persistent vector.

;; (spit
;; "src/pfchangsindex/raw-out.txt"
;;  (pfchangsindex.api-query/req-vector radius (:lat avillePFC) (:lon avillePFC)))
(def places (first
             (rest
              (pfchangsindex.api-query/get-places
               radius
               (:lat avillePFC)
               (:lon avillePFC)))))

;;(def places-list (pfchangsindex.api-query/get-places
;;              radius
;;              (:lat avillePFC)
;;              (:lon avillePFC)))


(println "number of places returned by query: " (count places))

(def aville-pfci (pfchangsindex.index-generator/gen-index places))
(println "the asheville PFC index for a radius of 1000m is: " aville-pfci)

;;(pfchangsindex.db/createdb)

;;(for [place (req-vector)]
;;  (pfchangsindex.db/transact place))

;;(clojure.pprint/pprint places-vec)

;;(pfchangsindex.db/query)
;;(pfchangsindex.db/del-db)
