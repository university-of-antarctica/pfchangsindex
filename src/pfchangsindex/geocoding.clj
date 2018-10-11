(ns pfchangsindex.geocoding
    (:require
      [geocoder.geonames :as geonames]
      [clojure.zip :as z]
      [pfchangsindex.resource_provider :as provider]
      [pfchangsindex.pfchangs_provider :as pfchangs]
      [pfchangsindex.api_query :as api_query]))

(def necessary-request-data [:results :status])
(def geocode-endpoint
  "https://maps.googleapis.com/maps/api/geocode/json?")

(defn req-to-map
  [req]
  (provider/extract-keys req necessary-request-data))

(defn raw-req
  [address]
  (api_query/issue-request geocode-endpoint {:address address}))

(defn get-geocode-data-with-address
  "string => map {:lat :lng}
  takes a properly formatted street address
  (https://developers.google.com/maps/documentation/geocoding/intro)
  and returns coordinates."
  [address]
  (let [req (raw-req address)
        request-map (req-to-map req)]
    request-map))

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
(def a-req (select-keys (clojure.walk/keywordize-keys (provider/get-stored-data geocoding-out)) necessary-request-data))
(def pf-addr (first (pfchangs/extract-pfchangs-address-vec)))
;;(get-geocode-data-with-address pf-addr)
;;(println (get-lat-lng-from-address pf-addr))
(println (get-lat-lng-from-seq pf-addr))

;;(println (get-lat-lng-from-seq a-req))
;;(println (get-key-from-seq :lng a-req))
(println a-req)

(get-lat-lng-from-seq a-req)

(comment write-address-to-file
 (provider/write-to-file
    geocoding-out
    (fn []
      (api_query/issue-request
        geocode-endpoint
        (provider/get-stored-data sample-address)))))

(comment
  (select-keys
    (clojure.walk/keywordize-keys (provider/get-stored-data geocoding-out) )
    necessary-request-data)
  (select-keys
    (get-geocode-data-with-address (first (pfchangs/extract-pfchangs-address-vec)))
    necessary-request-data))


