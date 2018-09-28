(ns pfchangsindex.resource_provider
  (:require [clojure.java.io :as io]
            [clojure.data.json :as json]))

(defn get-serialized-data
  [filename]
  (-> (io/resource filename)
      slurp
      read-string))

(defn get-json
  [filename]
  (-> (io/resource filename)
      slurp
      json/read-str))

(defn extract-keys
  [item keyvec]
  (map #(select-keys %1 keyvec) item))

