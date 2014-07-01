(ns todo-manager.data-handler.reader
   (:require [clojure.java.io :refer [reader]]
             [clojure.string :refer [join split trim blank?]]
             [clj-time.core :refer [date-time default-time-zone]]
             [clj-time.format :refer [formatter parse]]))


(defn parse-tags
  [value]
  (if (or (nil? value)) #{}
    (->> (split value #",")
         (map trim)
         (set))))

(defn parse-time
  [date]
  (parse (formatter (default-time-zone)
                    "YYYY/MM/dd" "YYYY-MM-dd")
         date))

(defn deserialize
  [[str-key value]]
  (let [key (keyword (trim str-key))
        value (trim value)
        value (condp = key
                :goal value
                :tags (parse-tags value)
                :priority (read-string value)
                :status (keyword value)
                :progress (read-string value)
                :end_date (parse-time value)
                :start_date (parse-time value))]
    [key value]))

(defn parse-todo
  [input]
  (if (blank? input) nil
    (->> (split (trim input) #";")
       (map #(deserialize (split % #":")))
       (into {}))))

(defn parse-collection
  [file-path]
  (with-open [rdr (reader file-path)]
  (doall (filter #((complement nil?) %)
                 (map parse-todo (line-seq rdr))))))