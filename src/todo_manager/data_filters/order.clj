(ns todo-manager.data-filters.order)

(def priority-comparator
  (fn [{priority1 :priority} {priority2 :priority}]
    (>= priority1 priority2)))

(def progress-comparator
  (fn [{progress1 :progress} {progress2 :progress}]
    (>= progress1 progress2)))


(defn lazy-qsort [[pivot & xs] comparator-fn]
  (when pivot
    (let [bigger #(comparator-fn % pivot)]
      (lazy-cat (lazy-qsort (filter bigger xs)
                            comparator-fn)
                [pivot]
                (lazy-qsort (remove bigger xs)
                            comparator-fn)))))

(defn order-by-priority
  [coll]
  (lazy-qsort (vec coll) priority-comparator))

(defn order-by-progress
  [coll]
  (lazy-qsort (vec coll) progress-comparator))