(ns pfchangsindex.api_query
  (:require [clj-http.client :as client]
            [clojure.data.json :as json]
            [clojure.walk :as walk]
            [pfchangsindex.resource_provider :as provider]
            [clojure.java.io :as io]
            [places.search :refer [nearby-search details]]))

(def goo-places-outfile "goog-places.out")

(def place-data-to-extract
  [:place_id :name :geometry :rating :photos :price_level])
(def nearbysearch-endpoint
  "https://maps.googleapis.com/maps/api/place/nearbysearch/json?")

(defn issue-request
  "string, map => response string
  Issues a request to url with given params."
  [url params]
  (let [secret-params (assoc params :key (provider/get-key))
        request (client/get url {:query-params secret-params })
        request-body (:body request)]
    (json/read-str request-body)))

(defn my-nearby-search
  "lng lat radius => map
  Call inferior wrapper for google places api"
  [lng lat radius] ;;
  (nearby-search (provider/get-key)
                 {:lng lng :lat lat}
                 :radius radius
                 :rankby "distance"
                 :types ["restaurant" "food"]));; :keyword "restaurant"))

(comment (provider/write-to-file
    goo-places-outfile
    (fn []  (my-nearby-search "-82.556" "35.484" 8064))) )

(provider/extract-keys
  (provider/get-stored-data goo-places-outfile)
  place-data-to-extract)
