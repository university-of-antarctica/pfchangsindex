(ns pfchangsindex.index_generator)

(def pfchangs-regex #"(?i)p.?f.? ?chang'?s")
(def pfchangs-index :pfchangs_index)
(def regions-key :regions)
(def rstrnt-key :restaurants)
(def base-edn-map {regions-key '[] rstrnt-key '[]})

(defn pfchangs?
  [restaurant]
  (let [name (:name restaurant)
        p (clojure.pprint/pprint restaurant)
        ty (println "type: " (type restaurant))]
   (if (nil? name)
    (throw (Exception. (str "why was the name nil")))
    (boolean (re-find pfchangs-regex name)))))

;;(println " found: " (boolean (re-find #"(?i)p.?f.? ?chang'?s" "PFchang's")))

;;(println " found: " (boolean (re-find #"(?i)p.?f.? ?chang'?s" nil)))
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
        :id (str (.hashCode (apply str "region-" (:place_id restaurant))))
        :pfchangs_place_id (:place_id restaurant)}
       (recur other-restaurants (inc index))))))

(defn gen-index
  "returns => int OR nil
  Returns the place that PF Changs occupies in the array.
  Returns nil if a PF Changs was not in restaurants"
  [restaurants]
  ;; It is important that 0 is passed. PF Chang's element number is
  ;; also the number of restaurants rated more highly than PF Chang's
  ;; in the list.
  (let [restaurants (try
                      (sort-by :rating > restaurants)
                      (catch Exception e (str "caught exception: " (.getMessage e)))
                      (finally restaurants))]
    (let [idx (find-pfchangs restaurants 0)
          id (:pfchangs_place_id idx)]
      (if (nil? id)
        (throw (RuntimeException. "Found no pf changs!"))
        idx))))

(defn decorate-restaurant
  [restaurant region-id]
  (let [restaurant (clojure.walk/keywordize-keys restaurant)
        is-pf (pfchangs? restaurant)
        p (println "ABOUT TO PRINT RESTAURANT" (:place_id restaurant))
        prest (clojure.pprint/pprint restaurant)
        p (println "DONE PRINT RESTAURANT" (:place_id restaurant))
        pf-id (str (.hashCode (:place_id restaurant)))
        new-restaurant-data {:id pf-id
                        regions-key #{region-id}
                        :is_pf_changs is-pf}]
         (merge restaurant new-restaurant-data)))

(defn add-restaurant-data
  [restaurant-map restaurants region-id]
  (loop [restaurant-map restaurant-map
         restaurants restaurants
         count 0]
   (let [r (first restaurants)
         p (println "pass: "  count ", " (:name (first restaurants)))]
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
        ann (println "resturants")
        print-restaurants (clojure.pprint/pprint restaurants)
        ann2 (println "region-data")
        print-region (clojure.pprint/pprint region-data)
        ann3 (println "region-id" region-id)
        restaurant-map (assoc restaurant-map
                            regions-key
                            (conj
                             (regions-key restaurant-map)
                             region-data))]
    (add-restaurant-data restaurant-map restaurants region-id)))

(defn build-pfchangs-schema
  [coll val]
  (build-edn-data coll val))

(comment ""
(pfchangsindex.resource_provider/write-map-to-edn
  pfchangsindex.core/all-pfcs-region-data-edn
  (let [build-edn-data (partial build-edn-data base-edn-map)
        datas (list (first pfchangsindex.core/raw-pfc-regions))]
    (map build-edn-data datas))))
