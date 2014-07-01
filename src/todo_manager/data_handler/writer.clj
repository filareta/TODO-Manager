(ns todo-manager.data-handler.writer
  (:require [clojure.java.io :refer [writer]]
            [clojure.string :refer [join split trim]]
            [clj-time.core :refer [date-time default-time-zone]]
            [clj-time.format :refer [formatter unparse]]))


(defn write-line
  [file-path keep-all line]
  (with-open [wtr (writer file-path :append keep-all)]
    (.write wtr (str line "\n"))))

(defn unparse-time
  [date]
  (unparse (formatter (default-time-zone)
                      "YYYY/MM/dd" "YYYY-MM-dd")
           date))

(defn serialize-entry
  [[key value]]
  (let [value
        (cond
          (= key :status) (name value)
          (= key :tags) (join ", " value)
          (or (= key :start_date) (= key :end_date))
            (unparse-time value)
          :else value)]
    (str (name key) ": " value "; ")))

(defn write-todo
  [{goal :goal start_date :start_date
    end_date :end_date priority :priority :as todo}
   filename]
  (->> todo
       (map serialize-entry)
       (join)
       (write-line filename true)))

(defn serialize-todo
  [todo]
  (->> todo
       (map serialize-entry)
       (join)))

(defn write-lines
  [file-path keep-all separator lines]
  (with-open [wtr (writer file-path :append keep-all)]
    (doseq [line lines] (.write wtr (str line separator)))))

(defn write-collection
  [todos filename]
  (->> todos
       (map serialize-todo)
       (write-lines filename false "\n")))