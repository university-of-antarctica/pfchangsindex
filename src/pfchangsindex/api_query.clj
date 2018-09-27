(ns pfchangsindex.api_query
  (:require [clj-http.client :as client]
            [clojure.data.json :as json]
            [clojure.walk :as walk]
            [clojure.java.io :as io]
            [places.search :refer [nearby-search details]]))

(def place-data-to-extract [:place_id :name :geometry :rating :photos :price_level])
(def goo-places-outfile "goog-places.out")

(defn get-key
  ;; fetches google api key
  []
  (->
    (io/resource "places-api-key.txt")
    slurp ))

(defn extract-google-places-with-json
  "json => vector
  takes json output from google's places api and parses it into places map"
  [json]
  (extract-places json))

(defn extract-output-from-file
  []
  (-> (io/resource goo-places-outfile)
      slurp
      read-string))

(defn extract-places-json-from-file
  "file in reousrce folder => json"
  []
  (-> extract-output-from-file
      json/read-str))

(defn my-nearby-search
  ;; call google places api
  [lng lat radius] ;;
  (nearby-search (get-key)
                 {:lng lng :lat lat}
                 :radius radius
                 :rankby "distance"
                 :types ["restaurant" "food"]));; :keyword "restaurant"))

(defn write-to-file
  "writes output of function to file"
  [search]
  (spit (io/resource goo-places-outfile) (pr-str (search))))

(write-to-file (fn []  (my-nearby-search "-82.556" "35.484" 8064)))

(defn extract-place
  "returns => vector
  Gets the keys we want in the vector we will be manipulating and storing in a db
  out of the results vector we get from the api."
  [places]
  (map #(walk/keywordize-keys %) (map #(select-keys % place-data-to-extract) places)))

(defn extract-keys
  [item]
  (map #(select-keys %1 place-data-to-extract) item))

(clojure.pprint/pprint (first (extract-output-from-file)) )
(extract-keys (extract-output-from-file))
