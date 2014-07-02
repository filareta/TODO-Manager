(ns todo-manager.test-reader
  (:require [clojure.test :refer :all]
            [todo-manager.data-handler.reader :refer :all]
            [clj-time.local :refer [to-local-date-time]]
            [clj-time.core :refer [date-time]]))


(deftest todo-reader-test
  (let [todo "status: in-progress; progress: 0.6;
            goal: Research for my project;
            end_date: 2014/07/05; start_date: 2014/06/24;
            priority: 5; tags: clojure, exam;"
        expected-parsed-todo
        {:goal "Research for my project"
         :progress 0.6
         :priority 5
         :status :in-progress
         :tags #{"clojure" "exam"}
         :start_date (to-local-date-time (date-time 2014 6 24))
         :end_date (to-local-date-time (date-time 2014 7 5))}
        expected-coll [expected-parsed-todo
                       {:goal "Voip exam"
                        :progress 0.2
                        :priority 7
                        :status :in-progress
                        :tags #{"voip" "exam"}
                        :start_date (to-local-date-time (date-time 2014 6 24))
                        :end_date (to-local-date-time (date-time 2014 7 5))}]]

    (testing "parse tags"
    (is (= (parse-tags "clojure, exam, clojure, july")
           #{"clojure" "exam" "july"}))
    (is (= (parse-tags "    ")
           #{}))
    (is (= (parse-tags nil)
           #{})))
  (testing "parse time"
    (is (= (parse-time "2014/07/04")
           (to-local-date-time (date-time 2014 7 4))))
    (is (= (parse-time "2014-07-08")
           (to-local-date-time (date-time 2014 7 8)))))
  (testing "parse todo"
    (is (= (parse-todo todo)
           expected-parsed-todo)))
  (testing "parse collection"
    (is (= (set (parse-collection "resources/test_reader.txt"))
           (set expected-coll))))))



