(ns todo-manager.data-handler.validator
  (:require [clojure.string
             :refer [join blank? trim]]))


(def error-messages
  {:start_date "Time format does not match YYYY/MM/DD or YYYY-MM-DD."
   :end_date "Time format does not match YYYY/MM/DD or YYYY-MM-DD."
   :status "Unknown status type."
   :progress "Progress must be a number from 0.0 to 1.0."
   :priority "Priority must be a number, e.g. 11."
   :blank "There are empty fields. Only tags are optional."})

(defn check-for-empty-fields
  [todo]
  (let [empty-fields (map (fn [[key value]] (blank? value))
                          (-> todo
                             (dissoc :tags)
                             (dissoc :status)))]
    (if (some true? empty-fields)
      (:blank error-messages))))

(defn validate-fields-content
  [todo]
  (let [date-matcher #(re-matches #"\d{4}(?:-|/)\d{2}(?:-|/)\d{2}" %)
        progress-matcher #(re-matches #"(?:0\.\d|1\.0)" %)
        priority-matcher #(re-matches #"\d+" %)
        errors-list (for [[key value] todo
                          :when (nil? (#{:goal :tags} key))]
                      (condp = key
                        :start_date (if (nil? (date-matcher value))
                                      (:start_date error-messages))
                        :end_date (if (nil? (date-matcher value))
                                    (:end_date error-messages))
                        :status (if (nil? (#{:new :in-progress :completed} value))
                                  (:status error-messages))
                        :progress (if (nil? (progress-matcher value))
                                    (:progress error-messages))
                        :priority (if (nil? (priority-matcher value))
                                    (:priority error-messages))))]
    (->> errors-list
         (join #" ")
         trim)))

(defn validate
  [todo]
  (let [blank-fields-info (check-for-empty-fields todo)]
    (if (nil? blank-fields-info)
      (validate-fields-content todo)
      blank-fields-info)))