(ns todo-manager.ui.main
  (:require [seesaw.core :as s]
            [todo-manager.data-handler.writer
             :refer [unparse-time]]
            [todo-manager.data-handler.storage
             :refer [delete-todo status-mapper
                     new-todos todos-in-progress completed-todos]]
            [clojure.string :refer [join]]))

(declare draw-collection)

(def frame (s/frame :title "TODO Manager" :height 640 :width 480
                    :resizable? true :visible? true :on-close :exit))

(def statuses (s/flow-panel :items [(s/checkbox :text "new")
                                   (s/checkbox :text "in-progress")
                                   (s/checkbox :text "completed")
                                   (s/checkbox :text "all" :selected? true)]
                           :align :center
                           :hgap 20 :vgap 20))

(def search-bar (s/horizontal-panel :items [(s/text  :text "Search your TODOs here!")
                                           (s/button :text "Search"
                                                     :halign :center
                                                     :valign :center)]))

(def add-todo (s/button :text "Create TODO"
                        :halign :center
                        :valign :center))

(def top (s/vertical-panel :items [statuses search-bar add-todo]))

(def panel (s/flow-panel :items [top]
                         :align :left
                         :vgap 10
                         :hgap 20))

(defn attach-todo-listeners
  [delete-button edit-button todo]
  (s/listen delete-button :action (fn [e] (delete-todo todo status-mapper)
                                    (s/config! panel :items (-> []
                                                                (into @todos-in-progress)
                                                                (into @new-todos)
                                                                (into @completed-todos)
                                                                (draw-collection)
                                                                (conj top))))))

(defn draw-todo
  [{status :status start_date :start_date end_date :end_date goal :goal
    priority :priority progress :progress tags :tags :as todo} id]
  (let [delete-button (s/button :text "Delete"
                                :id id
                                :halign :center
                                :valign :center
                                :class :delete)
        edit-button (s/button :text "Edit"
                              :id id
                              :halign :center
                              :valign :center
                              :class :edit)]
    (attach-todo-listeners delete-button edit-button todo)
    (s/vertical-panel :items [(s/label :text goal :id id
                                     :h-text-position :center
                                     :v-text-position :center)
                            (s/progress-bar :orientation :horizontal
                                            :value (* 100 progress))
                            (s/label :text (str "Start date: " (unparse-time start_date)))
                            (s/label :text (str "End date: " (unparse-time end_date)))
                            (s/label :text (str "Priority: " priority))
                            (s/label :text (str "Tags: " (join ", " tags)))
                            delete-button edit-button])))

(defn draw
  [todos]
  (let [items (draw-collection todos)]
    (-> frame
      (s/config! :content (s/config! panel :items (conj items top)))
      (s/show!))))

(defn draw-collection
  [todos]
  (let [counter 0
        items (for [todo todos]
                (draw-todo todo (inc counter)))]
    items))