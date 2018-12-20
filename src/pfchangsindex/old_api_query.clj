(ns pfchangsindex.old-api-query
  (:require [clj-http.client :as client]
            [clojure.data.json :as json]
            [fs-utils.core :as provider]
            [clojure.walk :as walk]))

;;TODO pretty solid chance it is a lot more trouble than it is worth
;;     keeping a lot of this data in a vector. Should consider changing
;;     to list when relevant.

(def secret-key-file "places-api-key.txt")

(def default-slp 4000) ;; Google needs this many ms to activate a next_page_token
(def important-keys ["place_id" "name" "geometry" "rating"])

(defn re-request-loc-data
  "returns => str
  The string it returns is a json object in string format. This is the subsequent
  request to the google api IF there are additional pages"
  [token]
  (client/get
    "https://maps.googleapis.com/maps/api/place/nearbysearch/json?"
    {:query-params {"key" (provider/get-key #(%) secret-key-file)
                    "pagetoken" token}}))

(defn request-loc-data
  "returns => str
  The string it returns is a json object in string format. This is the initial
  request to the google api."
  [radius lat lon]
  (client/get
   "https://maps.googleapis.com/maps/api/place/nearbysearch/json?"
    {:query-params {"location" (str lat "," lon)
                    "radius" (str radius)
                    "types" "restaurant"
                    "key" (provider/get-key #(%) secret-key-file)}}))

(defn extract-places
  "returns => vector
  Gets the keys we want in the vector we will be manipulating and storing in a db
  out of the results vector we get from the api."
  [places]
  (vec
   (filter
    :rating
    (map #(walk/keywordize-keys %) (map #(select-keys % important-keys) places)))))

(defn response-handler
  "returns => boolean
  returns true if we should submit another query and false otherwise"
  [status token]
  (case status
    "OK" (boolean token) ;; token will be nil if there is no next page
    "ZERO_REQUEST" false
    "OVER_QUERY_LIMIT" false
    "REQUEST_DENIED" false
    "INVALID_REQUEST" false))

(defn get-next-request
  "returns => '(int, vector)
  This function exists to help get-request. The google places api limits the
  number or results it sends per request to 20. Google will return up to 60 results
  so they let you get the next two pages by submitting the page token from the prev
  request (along with your key), this function allows us to ask for the next 2 pages.
  IFF the number of requests made is 3, then we need to indicate the more searches
  need to be done in the area."
  [prev-results token request-number]
  (Thread/sleep default-slp)
  (let [request (json/read-str (:body (re-request-loc-data token)))
        re-token (get request "next_page_token")
        status (get request "status")
        next-results (extract-places (get request "results"))
        full-results (vec (concat prev-results next-results))]

    (if (response-handler status re-token)
      (get-next-request full-results
                        re-token
                        (inc request-number))
      (list request-number full-results))))

(defn get-request
  "returns => '(int, vector)
  of all the results from the api call with the given parameter"
  [radius lat lon]
  (let [request (json/read-str (:body (request-loc-data radius lat lon)))
        status (get request "status")
        token (get request "next_page_token")
        results (extract-places (get request "results"))
        request-number 1]
    (if (response-handler status token)
      (get-next-request results
                        token
                        (inc request-number))
      (list request-number results))))

(defn extract-places-stored
  "returns => vector
  wrapper for fcn extract-places, returns stored query from file rather
  than querying google again."
  []
  (extract-places
   (json/read-str (slurp "src/pfchangsindex/raw-out.txt"))))

(defn get-places
 "returns => vector
  It returns a vector where each element is a map representing
  a place. If it is called with no args it returns stored data and
  if it is called with arguments than it gets the places-vector for
  the given radius around the given latitude and longitude."
  ([]
   (extract-places-stored))
  ([radius lat lon]
   (let [forceprint (println "key?: " (provider/get-key #(%) secret-key-file))]
     (get-request radius lat lon))))

(def radius 8064)
(def lat "35.484")
(def lng "-82.556")

(def cache-file (apply str "old-get-places" radius lat lng ".cache"))

(provider/cache
  (fn [] (pfchangsindex.old-api-query/get-places radius lat lng))
  cache-file)
