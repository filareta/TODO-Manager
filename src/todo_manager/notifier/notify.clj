(ns todo-manager.notifier.notify
  (:require [todo-manager.data-handler.storage
             :refer [todos-in-progress
                     new-todos
                     completed-todos]]
            [todo-manager.data-handler.reader
             :refer [parse-collection]]
            [clj-time.core :refer [minutes after? before?
                                   hours year month day]]
            [clj-time.local :refer [local-now]]
            [clj-time.periodic :refer [periodic-seq]]
            [clojure.string :refer [join]]))


(def time-distance (hours 24))
(def time-distance-minutes (minutes 1))

(def notification (atom ""))

(defn check-date?
  [date1 date2]
  (and (= (year date1) (year date2))
       (= (month date1) (month date2))
       (= (day date1) (day date2))))

(defn check-for-incomming-todos
  [coll]
  (filter #(let [date_to_check (second (periodic-seq (local-now)
                                                     time-distance))
                 start_date (:start_date %)]
            (check-date? date_to_check start_date))
          coll))

(defn check-for-todos-approaching-deadline
  [coll]
  (filter #(let [date_to_check (second (periodic-seq (local-now)
                                                     time-distance))
                 end_date (:end_date %)]
             (check-date? date_to_check end_date))
          coll))

(defn notify-for-todos-in-progress
 [coll]
 (filter #(let [current-date (local-now)
                start_date (:start_date %)
                end_date (:end_date %)]
            (and (after? current-date start_date)
                 (before? current-date end_date)))
         coll))

(defn build-notification
  [approaching-deadline incomming]
  (apply str (-> []
                 (conj (if (seq approaching-deadline)
                         "You have several TODOS approaching deadline!\n"
                         ""))
                 (conj (join "\n" approaching-deadline))
                 (conj "\n")
                 (conj (if (seq incomming)
                         "There a few TODOS to be started soon!\n"
                         ""))
                 (conj (join "\n" incomming)))))

(defn check-for-notifications
  []
  (while true
    (Thread/sleep 300000)
    (let [approaching-deadline
          (check-for-todos-approaching-deadline @todos-in-progress)
          incomming (check-for-incomming-todos @new-todos)]
      (if (or (seq incomming)
              (seq approaching-deadline))
        (reset! notification (build-notification (map #(:goal %) approaching-deadline)
                                                 (map #(:goal %) incomming)))))))