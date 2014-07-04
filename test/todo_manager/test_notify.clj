(ns todo-manager.test-notify
  (:require [clojure.test :refer :all]
            [todo-manager.notifier.notify :refer :all]
            [todo-manager.conf :refer [time-distance]]
            [clj-time.local :refer [to-local-date-time]]
            [clj-time.core :refer [date-time plus minus]]
            [clj-time.local :refer [local-now]]))


(deftest test-notifications
  (let [date (date-time 2014 7 9)
        not-equal-month (date-time 2014 8 9)
        not-equal-day (date-time 2014 7 4)
        equal-date (date-time 2014 7 9)
        todo1 {:goal "Research for my project"
             :progress 0.6
             :priority 5
             :status :in-progress
             :tags #{"clojure" "exam"}
             :start_date (to-local-date-time (date-time 2014 2 24))
             :end_date (to-local-date-time (date-time 2014 3 5))}
        todo2 {:goal "Voip exam"
                :progress 0.2
                :priority 7
                :status :in-progress
                :tags #{"voip" "exam"}
                :start_date (to-local-date-time (date-time 2014 1 24))
                :end_date (to-local-date-time (date-time 2014 3 5))}
        incomming-todo (-> todo1
                           (assoc :status :new)
                           (assoc :start_date
                                   (plus (local-now) time-distance)))
        incomming [incomming-todo (assoc todo2 :status :new)]
        no-incomming [(assoc todo1 :status :new)
                      (assoc todo2 :status :new)]
        approaching-deadline-todo (assoc todo1 :end_date
                                                (plus (local-now)
                                                      time-distance))
        approaching-deadline [approaching-deadline-todo todo2]
        no-approaching-deadline [todo1 todo2]
        todo-in-progress (-> todo1
                             (assoc :end_date
                                    (plus (local-now) time-distance))
                             (assoc :start_date
                                    (minus (local-now) time-distance)))
        in-progress [todo-in-progress todo2]
        no-notification [todo1 todo2]]

    (testing "date comparison"
      (is (= (check-date? date equal-date)
             true))
      (is (= (check-date? date not-equal-day)
             false))
      (is (= (check-date? date not-equal-month)
             false)))

    (testing "incomming todos"
      (is (= (check-for-incomming-todos incomming)
             [incomming-todo]))
      (is (= (check-for-incomming-todos no-incomming)
             [])))

    (testing "todos approaching deadline"
      (is (= (check-for-todos-approaching-deadline approaching-deadline)
             [approaching-deadline-todo]))
      (is (= (check-for-todos-approaching-deadline no-approaching-deadline)
             [])))

    (testing "todos in progress"
      (is (= (notify-for-todos-in-progress in-progress)
             [todo-in-progress]))
      (is (= (notify-for-todos-in-progress no-notification)
             [])))))