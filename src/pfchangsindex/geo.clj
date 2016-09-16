(ns pfchangsindex.geo
  (:require [clj-http.client :as client]
            [clojure.data.json :as json]))
;; doc link for places api!!!
;; https://developers.google.com/places/web-service/intro

;; common idiom thus far:
;; (defn places-req-copy []
;;  (json/read-str
;;    (slurp "places-req-out.txt")))

(def initial_sleep_time 500)

(defn create-place-map
  "returns => map
  operates on elements of vector from extract-places-vec to provide
  map of relevant data for a given place"
  [element]
  (let [id (get element "id")
        place_id (get element "place_id")
        name (get element "name")
        lat (get (get (get element "geometry") "location") "lat")
        lng (get (get (get element "geometry") "location") "lng")
        rating (get element "rating") ]
    (hash-map :id id :place_id place_id :name name :lat lat :lng lng :rating rating)))

(defn extract-places-vec
  "returns => vector
  This function takes two arguments, the first is a vector from the req-vector fcn,
  the second is an empty vector. Using the create-place-map fcn, it returns a vector
  of maps, where each map has pertinent information on each place"
  [vec acc_vec]
  (if (empty? vec)
    acc_vec
    (recur (rest vec) (conj acc_vec (create-place-map (first vec))))))

;; TODO define some env variable to eliminate path hardcoding
(defn extract-places-vec-stored
  "returns => vector
  wrapper for fcn extract-places-vec, returns stored query from file rather than
  quierying google again."
  []
  (extract-places-vec
   (json/read-str (slurp "/home/price/development/pfchangsindex/raw-out.txt"))
   (vector)))

(defn places-req
  "returns => str
  The string it returns is a json object in string format. This is the initial
  request to the google api."
  [radius lat lon]
  (client/get "https://maps.googleapis.com/maps/api/place/nearbysearch/json?"
              {:query-params {"location" (str lat "," lon)
                              "radius" (str radius)
                              "types" "restaurant"
                              "key" (slurp "src/pfchangsindex/places-api-key.txt")}}))
(defn places-re-req [token]
  "returns => str
  The string it returns is a json object in string format. This is the subsequent
  request to the google api IF there are additional pages"
  (client/get "https://maps.googleapis.com/maps/api/place/nearbysearch/json?"
              {:query-params {"key" (slurp "src/pfchangsindex/places-api-key.txt")
                              "pagetoken" token}}))

(defn concat-2vecs [src_vec add_vec]
  (into [] (concat src_vec add_vec)))

(defn extract-results-vec
  ;; returns a vector
  ""
  [request_body]
  (get (first (rest (rest request_body))) 1))

;; issue here is it APPEARS as if subsequent pages are bogus.
;; need to investigate further as I write the program to get
;; exhaustive list of restaurants in given area.

;; TODO this function is garbage and absolutely has bugs. Figure out
;; if the vec var is used properly. figure out why concat2vecs exists
;; and get rid of it
(defn get-subsq-pages-as-vecs [vec token count sleep_time]
  (Thread/sleep sleep_time)
  (let [request (json/read-str (:body (places-re-req token)))
        re-token (get request "next_page_token")
        status (get request "status")
        error (get request "error_message")]
    (println "re-status: " status)
    (println "re-token: " re-token)
    (println "re-error: " error)
    (println '(< count 9) ": " (boolean (< count 9)))
    (println '(.contains status "INVALID_REQUEST") ": " (boolean (.contains status "INVALID_REQUEST")))
    (println '(.contains status "OK") ": " (boolean (.contains status "OK")))
    (println '(clojure.string/blank? token) ": " (boolean (clojure.string/blank? token)))
    (println '(= nil token) ": " (boolean (= nil token)))
    (if (and ;;1 0 1 0 1
         (< count 9);; 1:1
         (or (.contains status "INVALID_REQUEST");; 0
             (not (and (.contains status "OK") ;; 0:: 1 1
                  (or (clojure.string/blank? re-token) (= nil re-token))))));; 1 0 1
      (case status
        "OK" (get-subsq-pages-as-vecs
              (concat-2vecs
               vec (extract-results-vec request))
              re-token (inc count) sleep_time)
        "ZERO_RESULTS" (concat-2vecs vec (extract-results-vec request))
        "OVER_QUERY_LIMIT" (concat-2vecs vec (extract-results-vec request))
        "REQUEST_DENIED" (get-subsq-pages-as-vecs
                          (concat-2vecs
                           vec (extract-results-vec request))
                          token (inc count) (+ sleep_time 200))
        "INVALID_REQUEST" (get-subsq-pages-as-vecs
                           (concat-2vecs
                             vec (extract-results-vec request))
                           token (inc count) (+ sleep_time 200)))
        (concat-2vecs vec (extract-results-vec request)))))
        ;;  (if (clojure.string/blank? token)
        ;;      (concat-2vecs vec (extract-results-vec request))

(defn req-vector [radius lat lon]
  (let [request (json/read-str (:body (places-req radius lat lon)))
        token (get request "next_page_token")
        status (get request "status")
        results (extract-results-vec request)]
    (println "-status: " status)
    (println "token: " token "  tokenIsBlank: " (clojure.string/blank? token))
    (println "type info about extract-results-vec: " (type results))
    results))
;;    (if (clojure.string/blank? token)
;;      ;; on true or false innermost statement, will return vector
;;      ;;TODO undo this concat-2vecs fcn
;;      results
;;      (get-subsq-pages-as-vecs results
;;                               token
;;                               (int 0)
;;                               initial_sleep_time))))
;;
