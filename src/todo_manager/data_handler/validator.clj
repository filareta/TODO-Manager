(ns todo-manager.data-handler.validator
  (:require [clojure.string :refer [join blank?]]))


(defn check-for-empty-fields
  [todo]
  (let [empty-fields (map (fn [[key value]] (blank? value))
                         (-> todo
                             (dissoc :tags)
                             (dissoc :status)))]
    (if (some true? empty-fields)
      "There are empty fields. Only tags are optional.")))

(defn validate-fields-content
  [todo]
  (let [date-matcher #(re-matches #"\d{4}(?:-|/)\d{2}(?:-|/)\d{2}" %)
        progress-matcher #(re-matches #"0\.\d" %)
        priority-matcher #(re-matches #"\d+" %)
        error-messages (for [[key value] todo
                             :when (nil? (#{:goal :tags} key))]
                        (condp = key
                          :start_date (if (nil? (date-matcher value))
                                        "Time format does not match YYYY/MM/DD or YYYY-MM-DD.")
                          :end_date (if (nil? (date-matcher value))
                                      "Time format does not match YYYY/MM/DD or YYYY-MM-DD.")
                          :status (if (nil? (#{:new :in-progress :completed} value))
                                    "Unknown status type.")
                          :progress (if (nil? (progress-matcher value))
                                      "Progress must be a number from 0.0 to 1.0")
                          :priority (if (nil? (priority-matcher value))
                                      "Priority must be a number, e.g. 11.")))]
    (join #" " error-messages)))

(defn validate
  [todo]
  (let [blank-fields-info (check-for-empty-fields todo)]
    (if (nil? blank-fields-info)
      (validate-fields-content todo)
      blank-fields-info)))