(ns todo-manager.test-writer
  (:require [clojure.test :refer :all]
            [todo-manager.data-handler.writer :refer :all]
            [todo-manager.data-handler.reader
             :refer [parse-collection]]
            [clj-time.local :refer [to-local-date-time]]
            [clj-time.core :refer [date-time]]))


(deftest todo-writer-test
  (let [local-time (to-local-date-time (date-time 2014 7 9))
        expected-string
        "goal: Research for my project; progress: 0.6; priority: 5; status: in-progress; tags: clojure, exam; start_date: 2014/06/24; end_date: 2014/07/05; "
        todo {:goal "Research for my project"
             :progress 0.6
             :priority 5
             :status :in-progress
             :tags #{"clojure" "exam"}
             :start_date (to-local-date-time (date-time 2014 6 24))
             :end_date (to-local-date-time (date-time 2014 7 5))}
        coll #{todo {:goal "Voip exam"
                    :progress 0.2
                    :priority 7
                    :status :in-progress
                    :tags #{"voip" "exam"}
                    :start_date (to-local-date-time (date-time 2014 6 24))
                    :end_date (to-local-date-time (date-time 2014 7 5))}}]

    (testing "unparse time"
      (is (or (= (unparse-time local-time)
                 "2014/07/09")
              (= (unparse-time local-time)
                 "2014-07-09"))))

    (testing "serialize todo"
      (is (= (serialize-todo todo)
             expected-string)))

    (testing "write collection"
      (write-collection coll "resources/test/test_writer.clj")
      (is (= (set (parse-collection "resources/test/test_writer.clj"))
             coll)))))