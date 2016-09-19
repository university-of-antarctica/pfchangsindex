(ns pfchangsindex.index-generator)

(def pfchangs "P.F. Chang's")

;;TODO is there a better return value for failure?
(defn find-pfchangs
  "returns => int
  Walks the vector until it finds the PF Changs, then returns that index, if
  PF Changs was not found it returns nil"
  [restaurants rank]
  (if (empty? restaurants)
    nil
    (if (= (:name (first restaurants)) pfchangs)
      rank
      (recur (rest restaurants) (inc rank)))))

(defn gen-index
  "returns => int
  Returns the place that PF Changs occupies in the array."
  [restaurants]
  (find-pfchangs (sort-by :rating > restaurants) 1))
