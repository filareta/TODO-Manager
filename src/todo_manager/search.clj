(ns todo-manager.search
  (:require [clojure.set :refer [union intersection
                                 difference]]
            [todo-manager.data-handler.storage
             :refer [new-todos
                     todos-in-progress
                     completed-todos]]))


(defn match-criterion?
  [[attr value] todo]
  (if (= attr :tag)
    ((:tags todo) value)
    (= (attr todo) value)))

(defn filter-by
  [criterion coll]
  (set (filter #(match-criterion? criterion %) coll)))

(defn search-todo
  [coll criteria]
  (condp = (first criteria)
    :and (reduce intersection (map #(search-todo coll %) (next criteria)))
    :or  (reduce union (map #(search-todo coll %) (next criteria)))
    :not (->> (map #(search-todo coll %) (next criteria))
              (reduce union)
              (difference (set coll)))
    (reduce intersection (set coll) (map #(filter-by % coll) criteria))))

(defn search-all
  [criteria]
  (-> [@new-todos @todos-in-progress @completed-todos]
      (flatten)
      (search-todo criteria)))

(def priority-comparator
  (comparator (fn [{priority1 :priority} {priority2 :priority}]
                (< priority1 priority2))))

(def progress-comparator
  (comparator (fn [{progress1 :progress} {progress2 :progress}]
                (< progress1 progress2))))


(defn lazy-qsort [[pivot & xs] comparator-fn]
  (when pivot
    (let [smaller #(comparator-fn % pivot)]
      (lazy-cat (lazy-qsort (filter smaller xs) comparator-fn)
                [pivot]
                (lazy-qsort (remove smaller xs) comparator-fn)))))

(defn order-by-priority
  [coll]
  (lazy-qsort coll priority-comparator))

(defn order-by-progress
  [coll]
  (lazy-qsort coll progress-comparator))