; (ns todo-manager.notify
;   (:import [org.apache.commons.daemon Daemon DaemonContext])
;   (:gen-class
;     :implements [org.apache.commons.daemon.Daemon])
;   (:require [todo-manager.data-handler.storage
;              :refer [todos-in-progress
;                      new-todos
;                      completed-todos]]
;             [todo-manager.data-handler.reader
;              :refer [parse-collection]]
;             [clj-time.core :refer [minutes after? before? hours year month day]]
;             [clj-time.local :refer [local-now]]
;             [clj-time.periodic :refer [periodic-seq]]
;             [clojure.string :refer [join]]
;             ))

; (def time-distance (hours 24))
; (def time-distance-minutes (minutes 1))

; (defn check-for-incomming-todos
;   []
;   (filter #(let [date_to_check (second (periodic-seq (local-now)
;                                                      time-distance))
;                  start_date (:start_date %)]
;             (and (= (year date_to_check) (year start_date))
;                  (= (month date_to_check) (month start_date))
;                  (= (day date_to_check) (day start_date))))
;           @new-todos))

; (defn notify-for-todos-in-progress
;  []
;  (filter #(let [current-date (local-now)
;                 start_date (:start_date %)
;                 end_date (:end_date %)]
;             (and (after? current-date start_date)
;                  (before? current-date end_date)))
;          @todos-in-progress))

; ;; A crude approximation of your application's state.
; (def state (atom {}))

; (defn init [args]
;   (swap! new-todos into (parse-collection "resources/new_todos.txt"))
;   ; (parse-collection "resources/completed_todos.txt")
;   (swap! todos-in-progress into (parse-collection "resources/todos_in_progress.txt"))
;   (swap! state assoc :running true)
;   (swap! state assoc :time (local-now)))

; (defn start []
;   (while (:running @state)
;     (if (#{(local-now)} (second (periodic-seq (:time @state)
;                                                   time-distance-minutes)))
;       (do
;         (println "You have work to do!!! These are your todos in progress!")
;         (println (join "\n====================================\n"
;                        (map #(:goal %) (notify-for-todos-in-progress))))

;         (let [incomming (check-for-incomming-todos)]
;           (if (seq incomming)
;             (do
;               (println "You have incomming todos!!!")
;               (println (join "\n====================================\n"
;                             (map #(:goal %) incomming))))))

;         (swap! state assoc :time (local-now))))
;   ; (Thread/sleep 5000)
;     ))

; (defn stop []
;   (swap! state assoc :running false))

; ;; Daemon implementation

; (defn -init [this ^DaemonContext context]
;   (init (.getArguments context)))

; (defn -start [this]
;   (future (start)))

; (defn -stop [this]
;   (stop))