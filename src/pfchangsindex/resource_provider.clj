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

(defn write-to-file
  "Writes output of function to file"
  [filename outputting]
  (spit (io/resource filename) (pr-str (outputting))))

(defn write-map-to-edn
  [filename my-map]
  (spit (io/resource filename (prn-str my-map))))

(defn get-key
  "Fetches google api key."
  []
  (->
    (io/resource "places-api-key.txt")
    slurp))

(defn get-stored-data
  [filename]
  (-> (io/resource filename)
      slurp
      read-string))
