(ns pfchangsindex.geocoding
    (:require
      [geocoder.geonames :as geonames]
      [clojure.zip :as z]
      [pfchangsindex.resource_provider :as provider]
      [pfchangsindex.pfchangs_provider :as pfchangs]
      [pfchangsindex.api_query :as api_query]))

(def necessary-response-data [:results :status])
(def geocode-endpoint
  "https://maps.googleapis.com/maps/api/geocode/json?")

(defn get-necessary-data-from-res
  "Take raw res from api and get keywordized map of relevant data."
  [res]
  (let [res-as-map (clojure.walk/keywordize-keys res)]
    (select-keys res-as-map necessary-response-data)))

(defn raw-req
  "Issue req to given geocode endpoint."
  [address]
  (api_query/issue-request geocode-endpoint {:address address}))

(defn get-geocode-data-with-address
  "string => map {:lat :lng}
  takes a properly formatted street address
  (https://developers.google.com/maps/documentation/geocoding/intro)
  and returns coordinates."
  [address]
  (let [res (raw-req address)
        res-map (get-necessary-data-from-res res)]
    res-map))

(defn get-key-from-seq
  "Given an arbitrary seq, traverse until value with given key appears"
  [k arbitrary-seq]
  (let [cc (z/zipper coll? seq nil arbitrary-seq)]
    (loop [x cc]
      (if (= (z/node x) k)
        (z/node (z/next x))
        (recur (z/next x))))))

(defn get-lat-lng-from-seq
  "Given a key and a seq, find :lat :lng values"
  [arbitrary-seq]
  (let [lat (get-key-from-seq :lat arbitrary-seq)
        lng (get-key-from-seq :lng arbitrary-seq)]
    {:lat lat :lng lng}))

(defn get-lat-lng-from-address
  "Given an address, return the :lat :lng values as a map."
  [address]
  (let [geocode-data (get-geocode-data-with-address address)
        lat-lng-map (get-lat-lng-from-seq geocode-data)]
    lat-lng-map))

(def geocoding-out "geocode-sample.out")
(def sample-address "sample-address.out")
(def sample-raw-res (select-keys
             (clojure.walk/keywordize-keys
               (provider/get-stored-data geocoding-out))
             necessary-response-data))

(def sample-pf-addr-lat-lng
  (get-lat-lng-from-address pfchangs/first-pfchang))

(println sample-pf-addr-lat-lng)

(comment "sample reads from file."
  (select-keys
    (clojure.walk/keywordize-keys (provider/get-stored-data geocoding-out) )
    necessary-response-data)
  (select-keys
    (get-geocode-data-with-address (first (pfchangs/extract-pfchangs-address-vec)))
    necessary-response-data))
