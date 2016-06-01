(ns pfchangsindex.geo
  (:require [clj-http.client :as client]
            [clojure.data.json :as json]))

;; common idiom thus far:
;; (defn places-req-copy []
;;  (json/read-str
;;   (slurp "places-req-out.txt")))

(def initial_sleep_time 500)

(defn create-place-map [element]
  (let [id (get element "id")
        place_id (get element "place_id")
        name (get element "name")
        lat (get (get (get element "geometry") "location") "lat")
        lng (get (get (get element "geometry") "location") "lng")
        rating (get element "rating") ]
    (hash-map :id id :place_id place_id :name name :lat lat :lng lng :rating rating)))

(defn extract-places-vec [vec acc_vec]
  (if (empty? vec)
    acc_vec
    (recur (rest vec) (conj acc_vec (create-place-map (first vec))))))

(defn places-req [radius lat lon]
  (client/get "https://maps.googleapis.com/maps/api/place/nearbysearch/json?"
              {:query-params {"key" (slurp "src/pfchangsindex/places-api-key.txt")
                              "location" (str lat "," lon)
                              "radius" (str radius)
                              "keyword" "restaurant"}}))

(defn places-re-req [token]
  (client/get "https://maps.googleapis.com/maps/api/place/nearbysearch/json?"
              {:query-params {"key" (slurp "src/pfchangsindex/places-api-key.txt")
                              "pagetoken" token}}))

(defn concat-2vecs [src_vec add_vec]
  (into [] (concat src_vec add_vec)))

(defn extract-results-vec [request_body] ;; returns a vector
  (get (first (rest (rest request_body))) 1))

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
            status (get request "status")]
        (println "-status: " status)
        (println "token: " token (clojure.string/blank? token))

        (if (clojure.string/blank? token) ;; innermost statement, will return vector
          ;; on true or false
          (concat-2vecs (extract-results-vec request) []) ;;TODO undo this concat-2vecs fcn
          (get-subsq-pages-as-vecs (extract-results-vec request) token (int 0) initial_sleep_time))))

