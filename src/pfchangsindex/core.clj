(ns pfchangsindex.core)

;; Instructions for calculating a PF Chang's Index.
;; 1. Find a PF Chang's (google places API) 
;; 2. Pick a radius
;; 3. Get the rating of every restaurant within that radius
;; 4. Order the restaurants by rating.
;; 5. The PF Chang's index for that area is the PF Chang's element number which is
;;    the number of restaurants rated more highly than PF Chang's.

;; It is an approximation of two important variables.
;; 1. The quality of the restaurants in a city.
;; 2. The quality of the restaurant culture in a city. Or more directly,
;;    whether or not the people in a given city have taste.

;; GLOBAL TODO
;; 1. Create a PF Changs index that uses google place API to churn out an index
;;    for the aville pf changs with a radius that returns < 60 but > 20 places,
;;    (still  requiring me to figure out the multipe page bologna).
;; 2. use yelp too: https://www.yelp.com/developers/documentation/v2/overview

;; doc link for places api!!!
;; https://developers.google.com/places/web-service/intro

(def avillePFC {:lat "35.48609758029149" :lon "-82.55266131970849"})
(def ask-google true) ;; T queries google, F uses text file.
(def radius "40000")

;; currently this spit is just here for testing purposes when I don't want to
;; waste google API calls. With ask-google set to false, when you define the
;; places-vec variable it calls extract-places-vec-stored which just slurps
;; the raw-out.txt file and puts it into a persistent vector.

;; (spit
;; "src/pfchangsindex/raw-out.txt"
;;  (pfchangsindex.api-query/req-vector radius (:lat avillePFC) (:lon avillePFC)))

(def places
  (if ask-google
    (pfchangsindex.api-query/get-places radius (:lat avillePFC) (:lon avillePFC))
    (pfchangsindex.api-query/get-places)))

(println "the count is: " (count (first (rest (pfchangsindex.api-query/get-places radius (:lat avillePFC) (:lon avillePFC))))))

(def aville-pfci (pfchangsindex.index-generator/gen-index places))
(println aville-pfci)

(defn places-vec-report
  "=> prints a string
  eventually we are going to need to know 2 things from google, 1 if there
  are additional pages of informationm and two if a query we did in a given
  area returned any NEW results, this is why we would contrast a vector with
  a set."
  [x]
  (let [vec_count (count x) set_count (count (into #{} x))]
    (println "vec-count: " vec_count " set_count: " set_count)))

(defn pprint-places-vec
  "=> prints a string
  easy way to pprint a sequence."
  [x]
  (dotimes [i (count x)] (clojure.pprint/pprint (get x i))))

(places-vec-report places-vec)

(pprint-places-vec places-vec)



;;(pfchangsindex.db/createdb)

;;(for [place (req-vector)]
;;  (pfchangsindex.db/transact place))

;;(clojure.pprint/pprint places-vec)

;;(pfchangsindex.db/query)
;;(pfchangsindex.db/del-db)
