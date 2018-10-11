(ns pfchangsindex.core
  (:require
    [clojure.data.json :as json]
    [clojure.java.io :as io]
    [pfchangsindex.pfchangs_provider :as pfchangs]
    [pfchangsindex.geocoding :as geo]
    [pfchangsindex.index_generator :as index_generator]))

(defn compute-pfci
  []
  (map geo/get-lat-lng-from-address (pfchangs/extract-pfchangs-address-vec)))

(println (first (pfchangs/extract-pfchangs-address-vec)))
(def pfci-list compute-pfci)

(clojure.walk/prewalk-demo pfci-list)
