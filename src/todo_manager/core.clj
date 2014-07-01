(ns todo-manager.core
  (:require [todo-manager.data-handler.writer
             :refer [write-collection]]
            [todo-manager.data-handler.reader
             :refer [parse-collection parse-time]]
            [todo-manager.data-handler.storage
             :refer [add-todo new-todos
                     todos-in-progress completed-todos
                     status-mapper delete-todo
                     mark-completed reopen]]
            [todo-manager.notifier.notify
             :refer [check-for-notifications notification]]
            [clojure.string :refer [join]]
            [clj-time.core :as t]
            [clj-time.periodic :as p]
            [clj-time.local :as l]
            [todo-manager.ui.main :as ui]))

(def todo1
  {:start_date "2014/07/12"
   :end_date "2014/07/23"
   :goal "Going to run"
   :priority "2"
   :status :new
   :progress "0.0"})

(def todo2
  {:start_date "2014/06/24"
   :end_date "2014/07/05"
   :goal "Writing my project"
   :priority "6"
   :tags "clojure, exam"
   :status :in-progress
   :progress "0.2"})

(def todo5
  {:start_date "2014/06/24"
   :end_date "2014/07/05"
   :goal "Research for my project"
   :priority "5"
   :tags "clojure, exam"
   :status :in-progress
   :progress "0.6"})

(def todo3
  {:start_date "2014/06/24"
   :end_date "2014/07/05"
   :goal "Voip exam"
   :priority "7"
   :tags "exam, voip"
   :status :in-progress
   :progress "0.2"})

(add-watch notification
           :new-notifications
           (fn [key reference old-state new-state]
             (ui/show-notification @reference)))

(defn -main
  "I don't do a whole lot."
  [& args]
  (swap! new-todos
         into (parse-collection "resources/new_todos.txt"))
  (swap! todos-in-progress
         into (parse-collection "resources/todos_in_progress.txt"))
  (swap! completed-todos
         into (parse-collection "resources/completed_todos.txt"))
  ; (add-todo todo5 status-mapper)
  ; (add-todo todo2 status-mapper)
  ; (add-todo todo1 status-mapper)
  ; (add-todo todo3 status-mapper)
  (ui/draw (-> []
               (into @todos-in-progress)
               (into @new-todos)
               (into @completed-todos)))
  (future (check-for-notifications)))
