(ns pfchangsindex.core
  (:gen-class)
  (:require
    [pfchangsindex.api_query :as api]
    [pfchangsindex.index_generator :as index]
    [pfchangsindex.resource_provider :as provider]
    [pfchangsindex.pfchangs_provider :as pfchangs]
    [pfchangsindex.geocoding :as geo]
    [clojure.edn :as edn]))

;;TODO add caching to call in api_query.

(def all-pfcs-latlng-outfile "all-pfcs-latlng-data.out")
(def all-pfcs-region-data-outfile "all-pfcs-region-data.out")
(def all-pfcs-region-data-edn "all-pfcs-region-data.edn")
(def transduce-pfcs-region-data-edn "transduce-pfcs-region-data.edn")
(def region-lookup-cache-suffix ".cache")
(def lat-lookup-cache-prefix "lat")
(def lng-lookup-cache-prefix "lng")

(defn get-all-pfc-latlngs
  []
  (provider/db
    #(map
      geo/get-lat-lng-from-address
      (pfchangs/extract-pfchangs-address-vec))
    all-pfcs-latlng-outfile))

(defn get-a-pfc-latlng
  []
   (first (get-all-pfc-latlngs)))

(defn get-n-pfc-latlngs
  [n]
  (take n (get-all-pfc-latlngs)))

(defn get-area-data
  [latlng radius]
  (provider/cache
   #(provider/extract-keys
    (api/my-nearby-search (:lng latlng) (:lat latlng) radius)
    api/place-data-to-extract)
   (let [filename (apply str
                         lat-lookup-cache-prefix (:lat latlng)
                         lng-lookup-cache-prefix (:lng latlng)
                         radius region-lookup-cache-suffix)
         a (println "filename : " filename)]
     filename)))

(defn a-region
  [radius latlng]
  (get-area-data latlng radius))

(defn region-argoud-radius
  [radius latlngs]
  (let [a-region (partial a-region radius)]
    (map a-region latlngs)))

(defn get-pf-changs-index
  [n]
  (conj
   '()
   (reduce
    index/build-pfchangs-schema
    index/base-edn-map
    (map (partial a-region 1024) (take n (get-all-pfc-latlngs))))))

(defn -main [n]
  (provider/db
    #(get-pf-changs-index (Integer/parseInt n))
    (apply str n all-pfcs-region-data-edn)))

(comment "gets 1 regions pf changs index"
         (provider/write-map-to-edn
          all-pfcs-region-data-edn
          (index/build-edn-data
           (first raw-pfc-regions)
           {:regions '[] :restaurants '[]})))

(comment "failed attempt at reduction, not properly reducing because our coll is a coll of colls"
         (provider/write-map-to-edn
          "myfile"
          (reduce
           (fn [val coll]
             (try
               (index/build-edn-data val coll)
               (catch Exception e (println "caught exception: " (.getMessage e)))
               (finally val)))
           {:regions '[] :restaurants '[]}
           (get-pfc-regions 1024 (get-n-pfc-latlngs 10)))))

(comment "failed attempt"
         (def xform
           (comp
            (map (partial a-region 1024))))
         (defn generate-pfc-edn-for-n-latlngs
           [n]
           (provider/from-root
            #(transduce xform (completing index/build-pfchangs-schema)
                        index/base-edn-map
                        (take n (get-all-pfc-latlngs)))
            transduce-pfcs-region-data-edn)))

(comment "also nope"
  (defn get-region
    [fn lat-lng]
      ((partial fn 1024) lat-lng))
  (defn get-first-pfchangs-region
    []
    (a-region 1024 (first (get-all-pfc-latlngs))))
  (transduce
  (map (partial get-region a-region))
  index/build-pfchangs-schema
  index/base-edn-map
  (take 1 (get-all-pfc-latlngs))))
  (comment
  (def xf
    (comp
      (map inc)
      (filter odd?)))
  (transduce xf + 0 (range 5)))
