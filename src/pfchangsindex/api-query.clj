(ns pfchangsindex.api-query
  (:require [clj-http.client :as client]
            [clojure.data.json :as json]))

(defn extract-raw-results-vec
  "returns => vector
  gets the body of the request from the json object the API returned
  * helper function for get-request-vector"
  [request_body]
  (get (first (rest (rest request_body))) 1))


;;TODO figure out what to do with the code from here {{{
(def initial_sleep_time 500)


(defn concat-2vecs [src_vec add_vec]
  (into [] (concat src_vec add_vec)))

(defn places-re-req [token]
  "returns => str
  The string it returns is a json object in string format. This is the subsequent
  request to the google api IF there are additional pages"
  (client/get "https://maps.googleapis.com/maps/api/place/nearbysearch/json?"
              {:query-params {"key" (slurp "src/pfchangsindex/places-api-key.txt")
                              "pagetoken" token}}))

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
               vec (extract-raw-results-vec request))
              re-token (inc count) sleep_time)
        "ZERO_RESULTS" (concat-2vecs vec (extract-raw-results-vec request))
        "OVER_QUERY_LIMIT" (concat-2vecs vec (extract-raw-results-vec request))
        "REQUEST_DENIED" (get-subsq-pages-as-vecs
                          (concat-2vecs
                           vec (extract-raw-results-vec request))
                          token (inc count) (+ sleep_time 200))
        "INVALID_REQUEST" (get-subsq-pages-as-vecs
                           (concat-2vecs
                             vec (extract-raw-results-vec request))
                           token (inc count) (+ sleep_time 200)))
        (concat-2vecs vec (extract-raw-results-vec request)))))
        ;;  (if (clojure.string/blank? token)
        ;;      (concat-2vecs vec (extract-results-vec request))


;;TODO UNTIL HERE


(defn request-loc-data
  "returns => str
  The string it returns is a json object in string format. This is the initial
  request to the google api. To get the data out of this raw request you need
  to pass the result to extract-places-vec thusly:
  (extract-places-vec (req))
  * helper function for get-request-vector"
  [radius lat lon]
  (client/get "https://maps.googleapis.com/maps/api/place/nearbysearch/json?"
              {:query-params {"location" (str lat "," lon)
                              "radius" (str radius)
                              "types" "restaurant"
;; TODO define some env variable to eliminate path hardcoding
                              "key" (slurp "src/pfchangsindex/places-api-key.txt")}}))

;;TODO trash this function and turn get-request vector into a multiple
;; arity fcn that can get the next page.
(defn get-next-page
  [prev-results token]
  (Thread/sleep 5000)
  (let [request (json/read-str (:body (places-re-req token)))
        re-token (get request "next_page_token")
        status (get request "status")
        error (get request "error_message")
        next-results (extract-raw-results-vec request)
        full-results (concat prev-results next-results)]
    (println "-status: " status)
    (println "REQUEST: " request)
    (println "token: " token "  tokenIsBlank: " (clojure.string/blank? re-token))
    (if (clojure.string/blank? token)
      full-results
      (get-next-page full-results
                     re-token))))

;; TODO investigate how I'm storing the data, google has a nice structure,
;; should I be wasting CPU cycles modifying it?
(defn get-request-vector
  "returns => vector
  of all the results from the api"
  [radius lat lon]
  (let [request (json/read-str (:body (request-loc-data radius lat lon)))
        token (get request "next_page_token")
        status (get request "status")
        results (extract-raw-results-vec request)]
    (println "status: " status)
    (println "REQUEST: " request)
    (println "token: " token "  tokenIsBlank: " (clojure.string/blank? token))
    (if (clojure.string/blank? token)
      results
      (get-next-page results
                     token))))

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
        rating (get element "rating")]
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

(defn extract-places-vec-stored
  "returns => vector
  wrapper for fcn extract-places-vec, returns stored query from file rather than
  quierying google again."
  []
  (extract-places-vec
   (json/read-str (slurp "src/pfchangsindex/raw-out.txt"))
   (vector)))

(defn get-places-vec
 "returns => vector
  To be precise it returns a vector where each element is a map representing
  a place. If it is called with no args it returns stored data and if it is called
  with arguments than it gets the places-vector for the given radius around the
  given latitude and longitude."
  ([]
   (extract-places-vec-stored))
  ([radius lat lon]
    (extract-places-vec (get-request-vector radius lat lon) (vector))))
