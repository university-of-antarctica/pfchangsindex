(ns pfchangsindex.resource_provider
  (:require [clojure.java.io :as io]
            [clojure.data.json :as json]
            [clojure.java.io :as jio])
  (:import (java.io File)))

(defn create-file
  [filename]
  (let [f (println "filename: " filename)]
    (.createNewFile (File. filename))))

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
  (let [f (println "getting file: " filename)]
    (-> (io/resource filename)
       slurp
       json/read-str)))

(defn extract-keys
  "Takes a map and extracts the keys in keyvec from it."
  [item keyvec]
  (map #(select-keys %1 keyvec) item))

(defn ensure-file-exists
  [filename]
  (let [resource (io/resource filename)]
    (if (and (not (nil? resource)) (.exists (io/file resource)))
      (io/resource filename)
      (do
        (create-file filename)
        (io/resource filename)))))

(defn write-to-file
  "Writes output of function to file"
  [filename outputting & opts]
  (spit (ensure-file-exists filename) (pr-str (outputting))))

(defn write-map-to-edn
  "Writes to filename my-map as a serialized data structure."
  [filename my-map]
  (spit (ensure-file-exists filename) (prn-str my-map)))

(defn get-cached-data-or-make-it
  [caching-fn possibly-cached-data]
  (let [data (get-serialized-data possibly-cached-data)]
    (if (nil? data)
      (let [my-map (caching-fn)]
        (write-map-to-edn possibly-cached-data my-map)
        my-map)
        (get-serialized-data possibly-cached-data))))

(defn cache-in-dir
  [dir caching-fn possibly-cached-data]
  (let [filename (if (.equals "" dir)
                   (str possibly-cached-data)
                   (str dir "/" possibly-cached-data))
        resource (ensure-file-exists filename)]
     (get-cached-data-or-make-it caching-fn filename)))

(defn cache
  [caching-fn possibly-cached-data]
    (let [p (println "call to cache: " possibly-cached-data)]
   (cache-in-dir "cache" caching-fn possibly-cached-data)))

(defn db
  [caching-fn possibly-cached-data]
  (let [p (println "call to db: " possibly-cached-data)]
    (cache-in-dir "db" caching-fn possibly-cached-data)))

(defn from-root
  [caching-fn possibly-cached-data]
  (let [p (println "call to root dir: " possibly-cached-data)]
    (cache-in-dir "" caching-fn possibly-cached-data)))

(defn get-key
  "Fetches google api key."
  []
  (->
    (ensure-file-exists "places-api-key.txt")
    slurp))
