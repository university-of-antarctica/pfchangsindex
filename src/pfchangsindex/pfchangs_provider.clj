(ns pfchangsindex.pfchangs_provider
    (:require
      [geocoder.geonames :as geonames]
      [pfchangsindex.resource_provider :as provider]))

(def pfchangs-location-data "locations.json")

(type get-pfchangs-json)

(defn entity-map
  [data k]
  (reduce #(assoc %1 (:id %2) %2)
          {}
          (get data k)))

(defn fetch-pfchangs
  ;; function that returns map of all pfchangs
  []
  (provider/get-json pfchangs-location-data))

(defn pprint-pfchangs
  ;; function to pprint list of all the pfchangs
  []
  (clojure.pprint/pprint (fetch-pfchangs)))

;; {:street_addrs [322 West Farms Mall Spc F226], :state CT, :city Farmington, :zip 06032}}
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
  (let [to-address (fn [item] (get-address-string (clojure.walk/keywordize-keys item)) )]
    (map
      to-address
      (-> (fetch-pfchangs)
          first
          rest
          first))))

;;(println (bing/geocode-address (get-address-string (first (extract-pfchangs-address-vec)) )) )
(println (geonames/geocode-address (first (extract-pfchangs-address-vec))) )
(geonames/geocode-address "Senefelderstraße 24, 10437 Berlin")