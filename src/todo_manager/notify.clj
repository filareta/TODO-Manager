(ns todo-manager.notify
  (:require [todo-manager.data-handler.storage
             :refer [todos-in-progress
                     new-todos]]
            [clj-time.core :refer [minutes after? before? hours year month day]]
            [clj-time.local :refer [local-now]]
            [clj-time.periodic :refer [periodic-seq]]
            ))

(def time-distance (hours 24))
(def time-distance-minutes (minutes 2))

(defn check-for-incomming-todos
  []
  (filter #(let [date_to_check (second (periodic-seq (local-now)
                                                     time-distance))
                 start_date (:start_date %)]
            (and (= (year date_to_check) (year start_date))
                 (= (month date_to_check) (month start_date))
                 (= (day date_to_check) (day start_date))))
          @new-todos))

(defn notify-for-todos-in-progress
 []
 (filter #(let [current-date (local-now)
                start_date (:start_date %)
                end_date (:end_date %)]
            (and (after? current-date start_date)
                 (before? current-date end_date)))
         @todos-in-progress))