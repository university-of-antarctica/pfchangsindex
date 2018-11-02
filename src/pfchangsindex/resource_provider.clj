(ns pfchangsindex.resource_provider
  (:require [clojure.java.io :as io]
            [clojure.data.json :as json]
            [clojure.java.io :as jio])
  (:import (java.io File)))

(defn create-file
  [filename]
  (.createNewFile (File. filename)))

(defn get-serialized-data
  "Reads from filename data that can be deserialized. If there
  is an exception this function returns nil."
  [filename]
  (try
    (-> (io/resource filename)
       slurp
       read-string)
    (catch Exception e nil)))

(defn get-json
  "Read json from filename."
  [filename]
  (-> (io/resource filename)
      slurp
      json/read-str))

(defn extract-keys
  "Takes a map and extracts the keys in keyvec from it."
  [item keyvec]
  (map #(select-keys %1 keyvec) item))

(defn write-to-file
  "Writes output of function to file"
  [filename outputting & opts]
  (spit (io/resource filename) (pr-str (outputting))))

(defn write-map-to-edn
  "Writes to filename my-map as a serialized data structure."
  [filename my-map]
  (spit (io/resource filename) (prn-str my-map)))

(defn get-cached-data-or-make-it
  [caching-fn possibly-cached-data]
  (let [data (get-serialized-data possibly-cached-data)]
    (if (nil? data)
      (let [my-map (caching-fn)]
        (write-map-to-edn possibly-cached-data my-map)
        my-map)
        (get-serialized-data possibly-cached-data))))

(defn cache
  [caching-fn possibly-cached-data]
  (let [filename (str "cache/" possibly-cached-data)
        resource (io/resource filename)]
    (if (and (not (nil? resource)) (.exists (io/file resource)))
     (get-cached-data-or-make-it caching-fn filename)
     (do
       (create-file filename)
       (get-cached-data-or-make-it caching-fn filename)))))

(comment "test to append stuff"
  (spit (io/resource "myfile")
    (prn-str {:alpha "bravo" :charlie "delta"})
    :append true))

;;(spit (io/resource "meowmoewbealkdjfd") "meowmeowmeowmewow99199131")

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
