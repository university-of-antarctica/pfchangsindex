(ns pfchangsindex.core
  (:require
    [pfchangsindex.api_query :as api]
    [pfchangsindex.index_generator :as index]
    [pfchangsindex.resource_provider :as provider]
    [pfchangsindex.pfchangs_provider :as pfchangs]
    [pfchangsindex.geocoding :as geo]))

(def all-pfcs-region-data-outfile "all-pfcs-region-data.out")

(defn get-pfc-latlngs
  []
  (map
   geo/get-lat-lng-from-address
   (pfchangs/extract-pfchangs-address-vec)))

(defn get-a-pfc-latlng
  []
  (geo/get-lat-lng-from-address (first (pfchangs/extract-pfchangs-address-vec))))

(defn get-pfchang-area-data
  [apfc radius]
  (provider/extract-keys
   (api/my-nearby-search (:lng apfc) (:lat apfc) radius)
   api/place-data-to-extract))

(defn a-pfc-region
  [radius pfc]
  (get-pfchang-area-data pfc radius))

(defn get-all-pfchangs-regions
  [radius]
  (let [a-pfc-region (partial a-pfc-region radius)]
    (map a-pfc-region (get-pfc-latlngs))))

(comment (provider/write-to-file
  all-pfcs-region-data-outfile
  (fn [] (get-all-pfchangs-regions 1024))))

(comment (provider/write-to-file
  all-pfcs-region-data-outfile
  (fn [] (a-pfc-region 1024 (get-a-pfc-latlng)))))

;;(clojure.pprint/pprint (a-pfc-region 8024 (get-a-pfc-latlng)))
;;(clojure.pprint/pprint (get-a-pfc-latlng))
;;(get-all-pfchangs-regions 1024)

;;(def raw-pfc-regions (provider/get-stored-data all-pfcs-region-data-outfile))
