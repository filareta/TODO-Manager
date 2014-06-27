(ns todo-manager.core
  (:import [org.apache.commons.daemon Daemon DaemonContext])
  (:gen-class
    :implements [org.apache.commons.daemon.Daemon])
  (:require [todo-manager.data-handler.writer
             :refer [write-collection]]
            [todo-manager.data-handler.reader
             :refer [parse-collection]]
            [todo-manager.data-handler.storage
             :refer [add-todo
                     new-todos
                     todos-in-progress
                     completed-todos
                     status-mapper
                     delete-todo
                     mark-completed
                     reopen]]
            [todo-manager.search
             :refer [search-todo
                     search-all
                     order-by-priority
                     order-by-progress]]
            [todo-manager.notify
             :refer [notify-for-todos-in-progress
                     check-for-incomming-todos]]

            [clj-time.core :as t]
            [clj-time.periodic :as p]
            [clj-time.local :as l]
            [clojure.string :refer [join]]))

;; A crude approximation of your application's state.
(def state (atom {}))

(defn init [args]
  (swap! new-todos into (parse-collection "resources/new_todos.txt"))
  ; (parse-collection "resources/completed_todos.txt")
  (swap! todos-in-progress into (parse-collection "resources/todos_in_progress.txt"))
  (swap! state assoc :running true)
  (swap! state assoc :time (l/local-now)))

(def time-distance (t/hours 24))
(def time-distance-minutes (t/minutes 1))

(defn start []
  (while (:running @state)
    (if(#{(l/local-now)} (second (p/periodic-seq (:time @state) time-distance-minutes)))
      (do
        (println "You have work to do!!! These are your todos in progress!")
        (println (join "\n====================================\n"
                       (map #(:goal %) (notify-for-todos-in-progress))))

        (let [incomming (check-for-incomming-todos)]
          (if (seq incomming)
            (do
              (println "You have incomming todos!!!")
              (println (join "\n====================================\n"
                            (map #(:goal %) incomming))))))

        (swap! state assoc :time (l/local-now))))
  ; (Thread/sleep 5000)
    ))

(defn stop []
  (swap! state assoc :running false))

;; Daemon implementation

(defn -init [this ^DaemonContext context]
  (init (.getArguments context)))

(defn -start [this]
  (future (start)))

(defn -stop [this]
  (stop))


(def todo1
  {:start_date "2014/07/12"
   :end_date "2014/07/23"
   :goal "Going to run"
   :priority 2
   :status :new
   :progress 0.0})

(def todo2
  {:start_date "2014/06/24"
   :end_date "2014/07/05"
   :goal "Writing my project"
   :priority 7
   :tags "clojure, exam"
   :status :in-progress
   :progress 0.2})

(def todo5
  {:start_date "2014/06/24"
   :end_date "2014/07/05"
   :goal "Research for my project"
   :priority 7
   :tags "clojure, exam"
   :status :in-progress
   :progress 0.6})

(def todo3
  {:start_date "2014/06/24"
   :end_date "2014/07/05"
   :goal "Voip exam"
   :priority 7
   :tags "exam, voip"
   :status :in-progress
   :progress 0.2})

(defn -main
  "I don't do a whole lot."
  [& args]
  (init args)
  ; (add-todo todo2 status-mapper)
  ; (add-todo todo1 status-mapper)
  ; (add-todo todo3 status-mapper)
  ; (add-todo todo5 status-mapper)
  (start)
  (println "NEW")
  (println @new-todos)
  (println "IN PROGRESS")
  (println @todos-in-progress)
  (println "COMPLETED")
  (println @completed-todos)
  (println (search-all [:or {:tag "exam"} {:priority 2}]))
  ; (println "Delete todo!!")
  ; (delete-todo (first (search-todo @new-todos {:goal (:goal todo1)})) status-mapper)
  (println @new-todos)
  ; (mark-completed (first (search-todo @todos-in-progress {:goal (:goal todo3)})) status-mapper)
  ; (println "After mark completed!")
  ; (println @completed-todos)
  (reopen (first (search-todo @todos-in-progress {:goal (:goal todo2)})) status-mapper)
  (println "After reopen!!")
  (println @new-todos)
  (println (order-by-priority @new-todos))
  (println (order-by-progress @todos-in-progress))
  )
