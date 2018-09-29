(ns pfchangsindex.geocoding
    (:require
      [geocoder.geonames :as geonames]
      [pfchangsindex.resource_provider :as provider]
      [pfchangsindex.api_query :as api_query]))

(def necessary-request-data [:results :status])
(def geocode-endpoint
  "https://maps.googleapis.com/maps/api/geocode/json?")
(def geocoding-out "geocode-sample.out")
(def sample-address "sample-address.out")

(defn get-geometry-from-address
  "string => map {:lat :lng}
  takes a properly formatted street address
  (https://developers.google.com/maps/documentation/geocoding/intro)
  and returns coordinates."
  [address]
  (let [raw-request (api_query/issue-request geocode-endpoint address)
        request-map (provider/extract-keys raw-request necessary-request-data)]
    (:location request-map)))

(comment (provider/write-to-file
    geocoding-out
    (fn []
      (api_query/issue-request
        geocode-endpoint
        {:address (get-stored-data provider/sample-address)}))))

(comment (provider/extract-keys
    (provider/get-stored-data geocoding-out)
    necessary-request-data) )

  (first (provider/get-stored-data geocoding-out) )
