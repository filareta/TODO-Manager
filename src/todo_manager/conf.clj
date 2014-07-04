(ns todo-manager.conf
  (:require [clj-time.core :refer [hours]]))


(def new-todos (atom #{}))
(def todos-in-progress (atom #{}))
(def completed-todos (atom #{}))

(def status-mapper {:new new-todos
                    :in-progress todos-in-progress
                    :completed completed-todos})

(def file-mapper {:new "resources/new_todos.txt"
                  :in-progress "resources/todos_in_progress.txt"
                  :completed "resources/completed_todos.txt"})

(def time-distance (hours 24))

(def time-period-to-notify 300000)
