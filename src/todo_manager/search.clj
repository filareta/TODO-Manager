(ns todo-manager.search
  (:require [clojure.set :refer [union intersection
                                 difference]]
            [clojure.contrib.string :refer [substring?]]
            [clojure.string :refer [lower-case split trim]]
            [todo-manager.data-handler.storage
             :refer [new-todos
                     todos-in-progress
                     completed-todos]]
            [todo-manager.data-handler.reader
             :refer [parse-time]]))


(defn match-criterion?
  [[attr value] todo]
  (cond
    (= attr :tag) ((:tags todo) value)
    (= attr :goal) (substring? (lower-case value) (lower-case (:goal todo)))
    :else (= (attr todo) value)))

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
              (difference coll))
    (reduce intersection coll (map #(filter-by % coll) criteria))))

(defn search-all
  [criteria]
  (let [all-todos (-> #{}
                      (into @todos-in-progress)
                      (into @new-todos)
                      (into @completed-todos))]
    (search-todo all-todos criteria)))

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
  (lazy-qsort (vec coll) priority-comparator))

(defn order-by-progress
  [coll]
  (lazy-qsort (vec coll) progress-comparator))

(defn add-time-criteria
  [words]
  (for [word words
        tag [:start_date :end_date]
        :when (re-matches #"\d{4}(?:-|/)\d{2}(?:-|/)\d{2}" word)]
    {tag (parse-time word)}))

(defn build-search-criteria
  [input conjuction]
  (let [words (map trim (split input #","))
        tags [:tag :goal]]
    (-> [conjuction]
        (into (for [word words
                    tag tags
                    :when (nil? (re-matches #"\d{4}(?:-|/)\d{2}(?:-|/)\d{2}" word))]
                {tag word}))
        (into (add-time-criteria words)))))