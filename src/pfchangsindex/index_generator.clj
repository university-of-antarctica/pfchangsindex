(ns pfchangsindex.index-generator)

(def pfchangs "P.F. Chang's")

;;TODO is there a better return value for failure?
(defn find-pfchangs
  "returns => int
  Walks the vector until it finds the PF Changs, then returns that index, if
  PF Changs was not found it returns nil"
  [restaurants index]
  (if (empty? restaurants)
    nil
    (if (= (:name (first restaurants)) pfchangs)
      index
      (recur (rest restaurants) (inc index)))))

(defn gen-index
  "returns => int
  Returns the place that PF Changs occupies in the array."
  [restaurants]
  ;; It is important that 0 is passed. PF Chang's element number is
  ;; also the number of restaurants rated more highly than PF Chang's
  ;; in the list.
  (find-pfchangs (sort-by :rating > restaurants) 0))
