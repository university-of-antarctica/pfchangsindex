(ns pfchangsindex.pfchangs_provider
    (:require
      [geocoder.geonames :as geonames]
      [pfchangsindex.resource_provider :as provider]))

(def pfchangs-location-data "locations.json")

(defn fetch-pfchangs
  ;; function that returns map of all pfchangs
  []
  (provider/get-json pfchangs-location-data))

(defn pprint-pfchangs
  ;; function to pprint list of all the pfchangs
  []
  (clojure.pprint/pprint (fetch-pfchangs)))

(def address-keys [:street_addrs :state :city :zip])

(defn get-address-string
  [map]
  (let [addr (:street_addrs map)
        state (:state map)
        city (:city map)
        zip (:zip map)]
    (str (first addr) ", " state ", " city " " zip)))

(defn extract-pfchangs-address-vec
  []
  (let [to-address (fn [item]
                    (get-address-string
                      (clojure.walk/keywordize-keys item)))]
    (map
      to-address
      (-> (fetch-pfchangs)
          first
          rest
          first))))

(def pfchangs (extract-pfchangs-address-vec))
(def first-pfchang (first (extract-pfchangs-address-vec)))
