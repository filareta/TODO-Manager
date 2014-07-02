(ns todo-manager.test-storage
  (:require [clojure.test :refer :all]
            [todo-manager.data-handler.storage :refer :all]
            [clj-time.local :refer [to-local-date-time]]
            [clj-time.core :refer [date-time]]))


(deftest todo-storage-test
  (let [test-new-todos (atom #{})
        test-todos-in-progress (atom #{})
        test-completed-todos (atom #{})
        test-status-mapper {:new test-new-todos
                            :in-progress test-todos-in-progress
                            :completed test-completed-todos}
        todo {:start_date "2014/06/24"
             :end_date "2014/07/05"
             :goal "Writing my project"
             :priority "6"
             :tags "clojure, exam"
             :status :in-progress
             :progress "0.6"}
        added-todo {:goal "Writing my project"
                   :progress 0.6
                   :priority 6
                   :status :in-progress
                   :tags #{"clojure" "exam"}
                   :start_date (to-local-date-time (date-time 2014 6 24))
                   :end_date (to-local-date-time (date-time 2014 7 5))}
        next-added-todo (assoc added-todo :goal "Start testing the project")
        next-todo (assoc todo :goal "Start testing the project")
        expected-completed (-> added-todo
                               (assoc :status :completed)
                               (assoc :progress 1.0))
        expected-new (-> added-todo
                         (assoc :status :new)
                         (assoc :progress 0.0))]
    (testing "add todo"
      (add-todo todo test-status-mapper)
      (is (= @test-todos-in-progress
             #{added-todo}))
      (add-todo todo test-status-mapper)
      (is (= (count @test-todos-in-progress) 1))
      (add-todo next-todo test-status-mapper))
      (is (= @test-todos-in-progress
             #{added-todo next-added-todo}))
      (is (= (count @test-todos-in-progress) 2))
    (testing "delete todo"
      (delete-todo added-todo test-status-mapper)
      (is (= (count @test-todos-in-progress) 1))
      (is (= @test-todos-in-progress
           #{next-added-todo}))
      (delete-todo (assoc added-todo :priority 11)
                   test-status-mapper)
      (is (= (count @test-todos-in-progress) 1))
      (delete-todo next-added-todo test-status-mapper)
      (is (= (count @test-todos-in-progress) 0)))
    (testing "mark completed"
      (add-todo todo test-status-mapper)
      (mark-completed added-todo test-status-mapper)
      (is (= (count @test-todos-in-progress) 0))
      (is (= (count @test-completed-todos) 1))
      (is (= (first @test-completed-todos)
             expected-completed)))
    (testing "reopen"
      (add-todo todo test-status-mapper)
      (reopen added-todo test-status-mapper)
      (is (= (count @test-todos-in-progress) 0))
      (is (= (count @test-new-todos) 1))
      (is (= (first @test-new-todos)
             expected-new)))))
