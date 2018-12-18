(ns pfchangsindex.index_generator
  (:require [fs-utils.core :as provider]))

(def pfchangs-regex #"(?i)p.?f.? ?chang'?s")
(def pfchangs-index :pfchangs_index)
(def pfchangs-place-id-key :pfchangs_place_id)
(def regions-key :regions)
(def rating-key :rating)
(def rstrnt-key :restaurants)
(def base-edn-map {regions-key '[] rstrnt-key '[]})

(defn get-positive-hash
  [in-str]
  (bit-and (.hashCode in-str) 0xFFFFFFF))

(defn pfchangs?
  [restaurant]
  (let [name (:name restaurant)]
   (if (nil? name)
    (throw (Exception. (str "why was the name nil")))
    (boolean (re-find pfchangs-regex name)))))

;;TODO is there a better return value for failure?
(defn find-pfchangs
  "returns => int
   Walks the vector until it finds the PF Changs, then returns that index, if PF Changs was not found it returns nil"
  [restaurants index]
  (let [restaurant (first restaurants)
        restaurant (clojure.walk/keywordize-keys restaurant)
        other-restaurants (rest restaurants)]
    (if (nil? restaurant)
     nil
     (if (pfchangs? restaurant)
         ;; stamp with pf_changs_index
       {:pfchangs_index index
        :id (str (get-positive-hash
                  (apply str "region-" (:place_id restaurant))))
        pfchangs-place-id-key (:place_id restaurant)}
       (recur other-restaurants (inc index))))))

(defn restaurants-lack-rating?
  [restaurants]
  (let [r (first restaurants)]
    (if (nil? r)
      true
      (if (nil? (rating-key r))
        false
        (recur (rest restaurants))))))

(defn gen-index
  "returns => int OR nil
  Returns the place that PF Changs occupies in the array.
  Returns nil if a PF Changs was not in restaurants"
  [restaurants]
  ;; It is important that 0 is passed. PF Chang's element number is
  ;; also the number of restaurants rated more highly than PF Chang's
  ;; in the list.
  (let [restaurants (filter #(contains? % rating-key) restaurants)
        restaurants (try
                      (sort-by rating-key > restaurants)
                      (catch Exception e
                        (str "caught exception: "
                            (.getMessage e)))
                      (finally restaurants))]
     (let [idx (find-pfchangs restaurants 0)
           id (pfchangs-place-id-key idx)]
       (if (nil? id)
         (throw (RuntimeException. "Found no pf changs!"))
         idx))))

(defn decorate-restaurant
  [restaurant region-id]
  (let [is-pf (pfchangs? restaurant)
        pf-id (str (get-positive-hash (:place_id restaurant)))
        new-restaurant-data {:id pf-id
                        regions-key #{region-id}
                        :is_pf_changs is-pf}]
         (merge restaurant new-restaurant-data)))

(defn add-restaurant-data
  [restaurant-map restaurants region-id]
  (loop [restaurant-map restaurant-map
         restaurants restaurants
         count 0]
   (let [r (first restaurants)]
     (if (nil? r)
       restaurant-map
       (recur (assoc restaurant-map
                     rstrnt-key
                     (conj
                      (rstrnt-key restaurant-map)
                      (decorate-restaurant r region-id)))
              (rest restaurants)
              (inc count))))))

(defn build-edn-data
  [restaurant-map restaurants]
  (let [region-data (gen-index restaurants)
        region-id (:id region-data)
        restaurant-map (assoc restaurant-map
                            regions-key
                            (conj
                             (regions-key restaurant-map)
                             region-data))]
    (add-restaurant-data restaurant-map restaurants region-id)))

(defn build-pfchangs-schema
  [coll val]
  (build-edn-data coll val))

(comment "test"
(provider/write-map-to-edn
  pfchangsindex.core/all-pfcs-region-data-edn
  (let [build-edn-data (partial build-edn-data base-edn-map)
        datas (list (first pfchangsindex.core/raw-pfc-regions))]
    (map build-edn-data datas))))
