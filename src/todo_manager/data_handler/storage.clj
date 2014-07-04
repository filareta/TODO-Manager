(ns todo-manager.data-handler.storage
  (:require [todo-manager.data-handler.writer
             :refer [write-collection]]
            [todo-manager.data-handler.reader
             :refer [parse-time parse-tags]]))


(def new-todos (atom #{}))
(def todos-in-progress (atom #{}))
(def completed-todos (atom #{}))

(def status-mapper {:new new-todos
                    :in-progress todos-in-progress
                    :completed completed-todos})

(def file-mapper {:new "resources/new_todos.txt"
                  :in-progress "resources/todos_in_progress.txt"
                  :completed "resources/completed_todos.txt"})

(add-watch new-todos
           :change-new-todos
           (fn [key reference old-state new-state]
             (write-collection @reference (:new file-mapper))))

(add-watch todos-in-progress
           :change-todos-in-progress
           (fn [key reference old-state new-state]
             (write-collection @reference (:in-progress file-mapper))))

(add-watch completed-todos
           :change-completed-todos
           (fn [key reference old-state new-state]
             (write-collection @reference (:completed file-mapper))))

(defn add-todo
  [{status :status end_date :end_date
    start_date :start_date tags :tags
    priority :priority progress :progress :as todo}
   mapper]
  (let [todo (-> todo
                (assoc :start_date (parse-time start_date))
                (assoc :end_date (parse-time end_date))
                (assoc :tags (parse-tags tags))
                (assoc :priority (read-string priority))
                (assoc :progress (read-string progress))
                (assoc :status (keyword status)))
        collection ((keyword status) mapper)]
    (swap! collection conj todo)))

(defn delete-todo
  [{status :status :as todo} mapper]
  (swap! (status mapper) (fn [coll]
                           (set (filter #(not= % todo) coll)))))

(defn change-status-progress
  [todo mapper status progress]
  (delete-todo todo mapper)
  (swap! (status mapper)
         conj
         (-> todo
             (assoc :status status)
             (assoc :progress progress))))

(defn mark-completed
  [todo mapper]
  (change-status-progress todo mapper :completed 1.0))

(defn reopen
  [todo mapper]
  (change-status-progress todo mapper :new 0.0))

(defn set-in-progress
  [todo mapper]
  (change-status-progress todo mapper :in-progress 0.0))