(ns pfchangsindex.index_generator)

(def pfchangs-name "P.F. Chang's")
(def pfchangs-index :pfchangs_index)
(def regions-key :regions)
(def rstrnt-key :restaurants)
(def base-edn-map {regions-key '[] rstrnt-key '[]})

(defn pfchangs?
  [restaurant]
  (try
    (.contains (:name restaurant) pfchangs)
    (catch Exception  e (str "caught exception: " (.getMessage e)))
    (finally false)))

;;TODO is there a better return value for failure?
(defn find-pfchangs
  "returns => int
  Walks the vector until it finds the PF Changs, then returns that index, if
  PF Changs was not found it returns nil"
  [restaurants index]
  (let [restaurant (first restaurants)
        other-restaurants (rest restaurants)]
    (if (nil? restaurant)
     '{}
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
    (find-pfchangs restaurants 0)))

(defn decorate-restaurant
  [restaurant region-id]
  (let [is-pf (pfchangs? restaurant)]
    (assoc (assoc (assoc restaurant :id (str (.hashCode (:place_id restaurant)))) :is_pf_changs is-pf) regions-key #{region-id})))

(defn build-edn-data
  [restaurant-map restaurants]
  (let [region-data (gen-index restaurants)
        region-id (:id region-data)
        restaurant-map (assoc restaurant-map
                            regions-key
                            (conj
                             (regions-key restaurant-map)
                             region-data))]
    (loop [restaurants restaurants
          restaurant-map restaurant-map]
      (let [r (first restaurants)]
        (if (nil? r)
         restaurant-map
         (recur (rest restaurants)
                (assoc restaurant-map
                       rstrnt-key
                      (conj
                        (rstrnt-key restaurant-map)
                        (decorate-restaurant r region-id)))))))))

(pfchangsindex.resource_provider/write-map-to-edn
 pfchangsindex.core/all-pfcs-region-data-edn
 (let [build-edn-data (partial build-edn-data base-edn-map)
       datas (list (first pfchangsindex.core/raw-pfc-regions))]
   (map build-edn-data datas)))
