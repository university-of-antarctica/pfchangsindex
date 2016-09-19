(ns pfchangsindex.api-query
  (:require [clj-http.client :as client]
            [clojure.data.json :as json]
            [clojure.walk :as walk]))

(def default-slp 5000) ;; Google needs this many ms to activate a next_page_token
(def important-keys ["place_id" "name" "geometry" "rating"])

(defn re-request-loc-data 
  "returns => str
  The string it returns is a json object in string format. This is the subsequent
  request to the google api IF there are additional pages"
  [token]
  (client/get "https://maps.googleapis.com/maps/api/place/nearbysearch/json?"
              {:query-params {"key" (slurp "src/pfchangsindex/places-api-key.txt")
                              "pagetoken" token}}))

(defn request-loc-data
  "returns => str
  The string it returns is a json object in string format. This is the initial
  request to the google api."
  [radius lat lon]
  (client/get "https://maps.googleapis.com/maps/api/place/nearbysearch/json?"
              {:query-params {"location" (str lat "," lon)
                              "radius" (str radius)
                              "types" "restaurant"
;; TODO define some env variable to eliminate path hardcoding
                              "key" (slurp "src/pfchangsindex/places-api-key.txt")}}))

(defn extract-places-vec
  "returns => vector
  Gets the keys we want in the vector we will be manipulating and storing in a db
  out of the results vector we get from the api."
  [places]
  ;;(filter "rating" (map #(select-keys % important-keys) vec)))
  (vec
   (filter
    :rating
    (map #(walk/keywordize-keys %) (map #(select-keys % important-keys) places)))))

;; TODO need to make get-next-request-vec and get-request-vec more dynamic so
;; they can handle different behaviors from google's places API.
(defn get-next-request-vector
  "returns => vector
  This function exists to help get-request-vector. The google places api limits the
  number or results it sends per request to 20. Google will return up to 60 results
  so they let you get the next two pages by submitting the page token from the prev
  request (along with your key), this function allows us to ask for the next 2 pages."
  [prev-results token default-slp]
  (Thread/sleep default-slp)
  (let [request (json/read-str (:body (re-request-loc-data token)))
        re-token (get request "next_page_token")
        status (get request "status")
        next-results (extract-places-vec (get request "results"))
        full-results (concat prev-results next-results)]
    (if (clojure.string/blank? token)
      full-results
      (get-next-request-vector full-results
                     re-token
                     default-slp))))

(defn get-request-vector
  "returns => vector
  of all the results from the api call with the given parameter"
  [radius lat lon]
  (let [request (json/read-str (:body (request-loc-data radius lat lon)))
        status (get request "status")
        token (get request "next_page_token")
        results (extract-places-vec (get request "results"))]
    (if (clojure.string/blank? token)
      results
      (get-next-request-vector results
                            token
                            default-slp))))

(defn extract-places-vec-stored
  "returns => vector
  wrapper for fcn extract-places-vec, returns stored query from file rather than
  querying google again."
  []
  (extract-places-vec
   (json/read-str (slurp "src/pfchangsindex/raw-out.txt"))))

(defn get-places-vec
 "returns => vector
  To be precise it returns a vector where each element is a map representing
  a place. If it is called with no args it returns stored data and if it is called
  with arguments than it gets the places-vector for the given radius around the
  given latitude and longitude."
  ([]
   (extract-places-vec-stored))
  ([radius lat lon]
    (get-request-vector radius lat lon)))

;;TODO change all names from *-vec once we know what we want the data to be and how to
;; deal with it
