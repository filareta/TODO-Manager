(ns todo-manager.core
  (:require [todo-manager.data-handler.reader
             :refer [parse-collection]]
            [todo-manager.conf
             :refer [new-todos
                     todos-in-progress
                     completed-todos
                     file-mapper]]
            [todo-manager.notifier.notify
             :refer [check-for-notifications
                     notification]]
            [todo-manager.ui.main :as ui]))


(add-watch notification
           :new-notifications
           (fn [key reference old-state new-state]
             (ui/show-notification @reference)))

(defn -main
  [& args]
  (swap! new-todos
         into (parse-collection (:new file-mapper)))
  (swap! todos-in-progress
         into (parse-collection (:in-progress file-mapper)))
  (swap! completed-todos
         into (parse-collection (:completed file-mapper)))
  (ui/draw (-> #{}
               (into @todos-in-progress)
               (into @new-todos)
               (into @completed-todos)))
  (future (check-for-notifications)))
