(ns pfchangsindex.geo
  (:require [clj-http.client :as client]
            [clojure.data.json :as json]))

(defn places-req-copy []
  (json/read-str
   (slurp "places-req-out.txt")))

(defn places-req [radius lat lon]
   (client/get "https://maps.googleapis.com/maps/api/place/nearbysearch/json?"
               {:query-params {"key" (slurp "src/pfchangsindex/places-api-key.txt")
                               "location" (str lat "," lon)
                               "radius" (str radius)
                               "keyword" "food"}}))

(defn places-re-req [token]
   (client/get "https://maps.googleapis.com/maps/api/place/nearbysearch/json?"
                {:query-params {"key" (slurp "src/pfchangsindex/places-api-key.txt")
                                "pagetoken" token}}))

(defn concat-vec [src_vec add_vec]
  (let [new_vec (into [] (concat src_vec add_vec))]
    (spit "raw-out" new-vec)
    new-vec))

(defn extract-places-vec [request_body] ;; returns a vector
  (get (first (rest (rest request_body))) 1))

(defn get-full-req-vector [vec token]
  (Thread/sleep 5000)
  (let [request (json/read-str (:body (places-re-req token)))
        re-token (get request "next_page_token")
        status (get request "status")
        error (get request "error_message")]
    (println "re-status: " status)
    (println "re-token: " re-token)
    (println "re-error: " error)
    (if (clojure.string/blank? re-token)
      (concat-vec vec (extract-places-vec request))
      (get-full-req-vector (concat-vec vec (extract-places-vec request)) token))))

(defn req-vector [radius lat lon]
  (let [request (json/read-str (:body (places-req radius lat lon)))
        token (get request "next_page_token")
        status (get request "status")]
    (println "-status: " status)
    (println "token: " token (clojure.string/blank? token))

    (if (clojure.string/blank? token) ;; innermost statement, will return vector
                                      ;; on true or false
      (concat-vec (extract-places-vec request) []) ;;TODO undo this concat-vec fcn
      (get-full-req-vector (extract-places-vec request) token))))

