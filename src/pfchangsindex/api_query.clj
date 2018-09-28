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

(defn get-stored-place-data
  []
  (-> (io/resource goo-places-outfile)
      slurp
      read-string))

(defn extract-places-json-from-file
  "file in reousrce folder => json"
  []
  (-> get-stored-place-data
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

(provider/extract-keys (get-stored-place-data) place-data-to-extract)
