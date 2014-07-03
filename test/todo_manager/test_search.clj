(ns todo-manager.test-search
  (:require [clojure.test :refer :all]
            [todo-manager.data-filters.search :refer :all]
            [todo-manager.data-handler.reader
             :refer [parse-collection parse-time]]
            [clj-time.local :refer [to-local-date-time]]
            [clj-time.core :refer [date-time plus minus]]))


(deftest test-search-todos
  (let [coll (set (parse-collection "resources/test/test_search.txt"))
        invoke-search (fn [criteria]
                        (set (map #(:goal %)
                                  (search-todo coll criteria))))
        date-criteria (to-local-date-time (date-time 2014 7 5))
        next-date-criteria (to-local-date-time (date-time 2014 6 25))]

    (testing "search todo by simple criteria"
      (is (= (invoke-search {:status :in-progress}))
          #{"Research for my project"
            "Training tae bo"
            "Writing my project"})
      (is (= (invoke-search {:end_date (parse-time "2014/07/05")})
             #{"Research for my project"
               "Writing my project"}))
      (is (= (invoke-search {:tag "sport"})
             #{"Going to run"
               "Training tae bo"})))

    (testing "search todo by complex criteria"
      (is (= (invoke-search [:and {:tag "sport"}
                                  {:status :in-progress}])
             #{"Training tae bo"}))
      (is (= (invoke-search [:and {:goal "going"}
                             [:not {:tag "pool"}]])
             #{"Going to run"}))
      (is (= (invoke-search [:and {:progress 0.8}
                                  {:priority 6}
                                  {:goal "run"}])
             #{}))
      (is (= (invoke-search [:or {:progress 0.8}
                                 {:priority 6}
                                 {:goal "run"}])
             #{"Training tae bo"
               "Writing my project"
               "Going to run"})))

    (testing "build criteria for searching by date"
      (is (= (add-time-criteria ["2014/07/05"
                                 "2014-06-25"])
             [{:start_date date-criteria}
              {:end_date date-criteria}
              {:start_date next-date-criteria}
              {:end_date next-date-criteria}]))
      (is (= (add-time-criteria ["sport" "clojure"])
             []))
      (is (= (add-time-criteria ["exam" "run" "2014/07/05"])
             [{:start_date date-criteria}
              {:end_date date-criteria}])))

    (testing "build simple search criteria from string"
      (is (= (build-search-criteria "exam, run, 2014/07/05" :or)
             [:or {:tag "exam"} {:goal "exam"}
                  {:tag "run"} {:goal "run"}
                  {:start_date date-criteria}
                  {:end_date date-criteria}]))
      (is (= (build-search-criteria "clojure, sport, 2014/07/05" :and)
             [:and {:tag "clojure"} {:goal "clojure"}
                   {:tag "sport"} {:goal "sport"}
                   {:start_date date-criteria}
                   {:end_date date-criteria}])))))


