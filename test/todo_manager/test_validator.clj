(ns todo-manager.test-validator
  (:require [clojure.test :refer :all]
            [todo-manager.data-handler.validator :refer :all]
            [clojure.string :refer [blank? join]]))


(deftest test-validator
  (let [todo {:start_date "2014/07/12"
             :end_date "2014/07/23"
             :goal "Going to run"
             :priority "2"
             :status :new
             :progress "0.0"
             :tags "sport"}
        todo-without-goal (assoc todo :goal nil)
        todo-without-tags (assoc todo :tags nil)
        invalid-todo (-> todo
                         (assoc :end_date "2014/09")
                         (assoc :progress "25"))]

    (testing "todo contains empty fields"
      (is (= (check-for-empty-fields todo-without-goal)
             (:blank error-messages))))

    (testing "tags are optional"
      (is (= (check-for-empty-fields todo-without-tags)
             nil)))

    (testing "validate date fields"
      (is (= (blank? (validate-fields-content todo))
             true))
      (is (= (validate-fields-content
               (assoc todo :start_date "2014/09"))
             (:start_date error-messages)))
      (is (= (validate-fields-content
               (assoc todo :start_date "2014:09:9"))
             (:start_date error-messages))))

    (testing "validate progress field"
      (is (= (validate-fields-content
               (assoc todo :progress "40"))
             (:progress error-messages)))
      (is (= (validate-fields-content
               (assoc todo :progress "1.6"))
             (:progress error-messages)))
      (is (= (blank? (validate-fields-content
                       (assoc todo :progress "1.0")))
             true))
      (is (= (blank? (validate-fields-content
                       (assoc todo :progress "0.7")))
             true)))

    (testing "validate priority field"
      (is (= (validate-fields-content
               (assoc todo :priority "A"))
             (:priority error-messages)))
      (is (= (blank? (validate-fields-content
                       (assoc todo :priority "7")))
             true))
      (is (= (validate-fields-content
               (assoc todo :priority "-5"))
             (:priority error-messages))))

    (testing "validate todo with empty fields"
      (is (= (validate todo-without-goal)
             (:blank error-messages))))

    (testing "validate todo with wrong field's content"
      (is (= (validate invalid-todo)
             (join #" " [(:progress error-messages)
                         (:end_date error-messages)]))))))