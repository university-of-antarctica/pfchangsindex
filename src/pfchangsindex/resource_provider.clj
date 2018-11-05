(ns pfchangsindex.resource_provider
  (:require [clojure.java.io :as io]
            [clojure.data.json :as json]
            [clojure.java.io :as jio])
  (:import (java.io File)))

(defn create-file
  [filename dir basefile]
    (try
      (let [
            parent-dir (io/resource dir)
            parent-path (.getFile parent-dir)
            filename (apply str parent-path "/" basefile)
            printfilename (println "create-file w/ filename: " filename)
            ]
        (.createNewFile (File. filename)))
      (catch Exception e (do
                           (println "caught ex: " (.getMessage e))
                           (clojure.pprint/pprint (.getStackTrace e))))
      (finally (.exists (File. filename)))))

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
  [filename dir basefile]
  (let [resource (io/resource filename)]
    (if (and (not (nil? resource)) (.exists (io/file resource)))
      (io/resource filename)
      (do
        (if (create-file filename dir basefile)
          (io/resource filename)
          (throw (RuntimeException. "Couldn't create file?")))))))

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
  (let [blank-dir? (.equals "" dir)
        filename (if blank-dir?
                   (str possibly-cached-data)
                   (str dir "/" possibly-cached-data))
        pr (println "cache-in-dir, blank-dir? : " blank-dir? ", dir: " dir ", filename: " filename)
        resource (ensure-file-exists filename dir possibly-cached-data)]
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
