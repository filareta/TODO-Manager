(ns todo-manager.test-order
  (:require [clojure.test :refer :all]
            [todo-manager.data-filters.order :refer :all]
            [todo-manager.data-handler.reader
             :refer [parse-collection]]))


(deftest test-ordering-todos
  (let [number-coll [7 9 -1 3 61 8]
        compare-fn (fn [a b] (>= a b))
        coll (set (parse-collection
                    "resources/test/test_order.txt"))
        priorities (fn [c] (map #(:priority %) c))
        progresses (fn [c] (map #(:progress %) c))
        goals (fn [c] (map #(:goal %) c))
        ordered-by-priority (order-by-priority coll)
        ordered-by-progress (order-by-progress coll)]

    (testing "simple sorting numbers"
      (is (= (lazy-qsort number-coll compare-fn)
             [61 9 8 7 3 -1])))

    (testing "order todos by priority"
      (is (= (priorities ordered-by-priority)
             [9 7 6 5 4]))
      (is (= (goals ordered-by-priority)
             ["Training tae bo"
              "Writing my project"
              "Going to run"
              "Research for my project"
              "Going to swim"])))
    (testing "order todos by progress"
      (is (= (progresses ordered-by-progress)
             [0.9 0.8 0.5 0.0 0.0]))
      (is (= (goals ordered-by-progress)
             ["Writing my project"
              "Training tae bo"
              "Research for my project"
              "Going to swim"
              "Going to run"])))))