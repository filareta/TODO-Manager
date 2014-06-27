(ns todo-manager.data-access.crud
  (:require [clojure.java.io :refer [writer reader]]
            [clojure.string :refer [join split trim]]))

(defn write-lines
  [file-path keep-all lines]
  (with-open [wtr (writer file-path :append keep-all)]
    (doseq [line lines] (.write wtr line))))

(defn write-line
  [file-path keep-all line]
  (with-open [wtr (writer file-path :append keep-all)]
    (.write wtr (str line "\n"))))

(defn attach-properties
  [todo]
  (merge todo {:status :new :progress 0.0 :tags []}))

(defn serialize-entry
  [[key value]]
  (let [value
        (cond
          (and (= key :tags) (seq value)) (join ", " value)
          (= key :status) (name value)
          :else value)]
    (str (name key) ": " value "; ")))

(defn write-todo-separated
  [{goal :goal start_date :start_date
    end_date :end_date priority :priority :as todo} seq-number]
  (->> (attach-properties todo)
      (map serialize-entry)
      (write-lines
        (str "resources/todos/todo" seq-number ".txt")
        false)))

(defn write-todo
  [{goal :goal start_date :start_date
    end_date :end_date priority :priority :as todo}]
  (->> (attach-properties todo)
       (map serialize-entry)
       (join)
       (write-line "resources/todos.txt" true)))

(defn parse-tags
  [value]
  [])

(defn deserialize
  [[str-key value]]
  (let [key (keyword (trim str-key))
        value (condp = key
                :goal (trim value)
                :tags (parse-tags (trim value))
                :priority (read-string (trim value))
                :status (keyword (trim value))
                :progress (read-string (trim value))
                :end_date (trim value)
                :start_date (trim value)
                (println key)
)]
    [key value]))

(defn parse-todo
  [input]
  (println input)
  (->> (split (trim input) #";")
       (map #(deserialize(split % #":")))
       (into {})))

(defn parse-todos
  [file-path]
  (with-open [rdr (reader file-path)]
  (doall (map parse-todo (line-seq rdr)))))