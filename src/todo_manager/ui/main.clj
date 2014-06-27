(ns todo-manager.ui.main
  (:require [seesaw.core :as s]
            [todo-manager.data-handler.writer
             :refer [unparse-time]]
            [clojure.string :refer [join]]))


(defn draw-todo
  [{status :status start_date :start_date end_date :end_date goal :goal
    priority :priority progress :progress tags :tags :as todo}]
  (s/vertical-panel :items [(s/label :text goal
                                      :h-text-position :center
                                      :v-text-position :center)
                            (s/progress-bar :orientation :horizontal
                                            :value (* 100 progress))
                            (s/label :text (str "Start date: " (unparse-time start_date)))
                            (s/label :text (str "End date: " (unparse-time end_date)))
                            (s/label :text (str "Priority: " priority))
                            (s/label :text (str "Tags: " (join ", " tags)))
                            (s/button :text "Delete"
                                      :halign :center
                                      :valign :center)
                            (s/button :text "Edit"
                                      :halign :center
                                      :valign :center)]))

(defn main-frame
  [todos]
  (let [frame (s/frame :title "TODO Manager" :height 640 :width 480
                       :resizable? true :visible? true :on-close :exit)
        items (for [todo todos]
                (draw-todo todo))
        search-bar (s/horizontal-panel :items [(s/text  :text "Search your TODOs here!")
                                               (s/button :text "Search"
                                                         :halign :center
                                                         :valign :center)])
        add-todo (s/button :text "Create TODO"
                           :halign :center
                           :valign :center)
        bottom (s/flow-panel :items [search-bar add-todo]
                             :align :center
                             :hgap 20 :vgap 20)
        statuses (s/flow-panel :items [(s/checkbox :text "new")
                                       (s/checkbox :text "in-progress")
                                       (s/checkbox :text "completed")
                                       (s/checkbox :text "all" :selected? true)]
                               :align :center
                               :hgap 20 :vgap 20)
        panel (s/flow-panel :items (-> statuses
                                       (cons items)
                                       vec
                                       (conj bottom))
                            :align :left
                            :vgap 10
                            :hgap 20)]
    (-> frame
        (s/config! :content panel)
        (s/show!))))