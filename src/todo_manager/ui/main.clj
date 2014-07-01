(ns todo-manager.ui.main
  (:require [seesaw.core :as s]
            [todo-manager.data-handler.writer
             :refer [unparse-time]]
            [todo-manager.data-handler.storage
             :refer [delete-todo status-mapper add-todo
                     new-todos todos-in-progress completed-todos]]
            [todo-manager.data-filters.search
             :refer [build-search-criteria search-all]]
            [clojure.string :refer [join]]))

(declare draw-collection)
(declare draw-todo)
(declare draw-form)

(def query-string (atom ""))
(def todo (atom {}))

(def frame (s/frame :title "TODO Manager" :height 640 :width 480
                    :resizable? true :visible? true :on-close :exit))

(def button-group (s/button-group))

(def statuses (s/flow-panel :items [(s/radio :text "new" :id :new
                                             :group button-group)
                                    (s/radio :text "in-progress" :id :in-progress
                                             :group button-group)
                                    (s/radio :text "completed" :id :completed
                                             :group button-group)
                                    (s/radio :text "all" :id :all :selected? true
                                             :group button-group)]
                           :align :center :hgap 20 :vgap 20))

(def search-bar (s/horizontal-panel :items
                                    [(s/text  :text "Search your TODOs here!"
                                              :id :input)
                                    (s/button :text "Search" :id :search-button
                                              :halign :center :valign :center)]))

(def add-todo-button (s/button :text "Create TODO"
                        :halign :center
                        :valign :center))

(def top (s/vertical-panel :items
                           [statuses search-bar
                            add-todo-button]))

(def panel (s/flow-panel :items [top]
                         :align :left
                         :vgap 10
                         :hgap 20))

(defn resolve-todos-type
  []
  (let [status (s/id-of (s/selection button-group))]
    (if (= status :all)
      (-> []
          (into @todos-in-progress)
          (into @new-todos)
          (into @completed-todos))
      @(status status-mapper))))

(def back-button
  (s/button :text "Back" :id :back
            :listen
            [:action
             (fn [e]
               (s/selection! button-group (s/select statuses [:#all]))
               (s/config! panel :items (-> (resolve-todos-type)
                                           (draw-collection)
                                           (conj top)))
               (s/config! frame :content panel)
               (s/show! frame))]))

(def create-button
  (s/button :text "Create" :id :create))

(defn attach-todo-listeners
  [delete-button edit-button todo]
  (s/listen delete-button
            :action
            (fn [e] (delete-todo todo status-mapper)
                    (s/config! panel :items (-> (resolve-todos-type)
                                                (draw-collection)
                                                (conj top)))))
  (s/listen edit-button
            :action
            (fn [e] (s/config! panel :items
                                     [(draw-todo todo) back-button]))))

(defn attach-status-listeners
  []
  (let [new-selector (s/select statuses [:#new])
        in-progress-selector (s/select statuses [:#in-progress])
        completed-selector (s/select statuses [:#completed])
        all-selector (s/select statuses [:#all])]
    (s/listen new-selector
            :action
            (fn [e]
              (s/selection! button-group new-selector)
              (s/config! panel :items (-> @new-todos
                                          (draw-collection)
                                          (conj top)))))
    (s/listen in-progress-selector
            :action
            (fn [e]
              (s/selection! button-group in-progress-selector)
              (s/config! panel :items (-> @todos-in-progress
                                          (draw-collection)
                                          (conj top)))))
    (s/listen completed-selector
            :action
            (fn [e]
              (s/selection! button-group completed-selector)
              (s/config! panel :items (-> @completed-todos
                                          (draw-collection)
                                          (conj top)))))
    (s/listen all-selector
            :action
            (fn [e]
              (s/selection! button-group all-selector)
              (s/config! panel :items (-> []
                                          (into @todos-in-progress)
                                          (into @new-todos)
                                          (into @completed-todos)
                                          (draw-collection)
                                          (conj top)))))))

(defn attach-search-listeners
  []
  (let [input-selector (s/select search-bar [:#input])
        button-selector (s/select search-bar [:#search-button])]
    (s/listen input-selector
              :insert-update
              (fn [d]
                (reset! query-string (s/text input-selector))))
    (s/listen button-selector
              :action
              (fn [e]
                (s/selection! button-group (s/select statuses [:#all]))
                (s/config! panel :items (-> @query-string
                                            (build-search-criteria :or)
                                            (search-all)
                                            vec
                                            (draw-collection)
                                            (conj top)))))))

(def form-button-group (s/button-group))

(defn attach-todo-form-listeners
  []
  (s/listen (s/select frame [:#goal])
              :insert-update
              (fn [e] (swap! todo assoc
                                  :goal
                                  (s/text (s/select frame [:#goal])))))
  (s/listen (s/select frame [:#start_date])
              :insert-update
              (fn [e] (swap! todo assoc
                                  :start_date
                                  (s/text (s/select frame [:#start_date])))))
  (s/listen (s/select frame [:#end_date])
              :insert-update
              (fn [e] (swap! todo assoc
                                  :end_date
                                  (s/text (s/select frame [:#end_date])))))
  (s/listen (s/select frame [:#priority])
              :insert-update
              (fn [e] (swap! todo assoc
                                  :priority
                                  (s/text (s/select frame [:#priority])))))
  (s/listen (s/select frame [:#progress])
              :insert-update
              (fn [e] (swap! todo assoc
                                  :progress
                                  (s/text (s/select frame [:#progress])))))
  (s/listen form-button-group
              :action
              (fn [e] (swap! todo assoc
                                  :status
                                  (s/text (s/selection form-button-group)))))
  (s/listen (s/select frame [:#tags])
              :insert-update
              (fn [e] (swap! todo assoc
                                  :tags
                                  (s/text (s/select frame [:#tags])))))
  (s/listen create-button
            :action
            (fn [e]
              (add-todo @todo status-mapper))))

(defn attach-create-listeners
  []
  (s/listen add-todo-button
            :action (fn [e]
                      (s/config! frame :content (draw-form))
                      (attach-todo-form-listeners))))

(defn draw-form
  []
  (let [properties [:goal :start_date :end_date :progress :priority :tags]
        property-panels (for [property properties]
                          (s/flow-panel :align :left
                                        :hgap 10
                                        :vgap 20
                                        :items [(s/label :text (name property))
                                                (s/text :id property :halign :left
                                                        :columns 40 :margin 15)]))
        status (s/flow-panel :items [(s/radio :text "new"
                                              :group form-button-group)
                                     (s/radio :text "in-progress"
                                              :group form-button-group)
                                     (s/radio :text "completed"
                                              :group form-button-group)]
                             :align :center
                             :hgap 20 :vgap 20)]
    (s/vertical-panel :id :todo-form
                      :items (-> property-panels
                                 vec
                                 (conj status)
                                 (conj create-button)
                                 (conj back-button)))))

(defn draw-todo
  [{status :status start_date :start_date
    end_date :end_date goal :goal
    priority :priority progress :progress
    tags :tags :as todo}]
  (let [delete-button (s/button :text "Delete"
                                :halign :center
                                :valign :center
                                :class :delete)
        edit-button (s/button :text "Edit"
                              :halign :center
                              :valign :center
                              :class :edit)]
    (attach-todo-listeners delete-button edit-button todo)
    (s/vertical-panel :items [(s/label :text goal
                                       :h-text-position :center
                                       :v-text-position :center)
                            (s/progress-bar :orientation :horizontal
                                            :value (* 100 progress))
                            (s/label :text (str "Start date: "
                                                (unparse-time start_date)))
                            (s/label :text (str "End date: "
                                                (unparse-time end_date)))
                            (s/label :text (str "Priority: "
                                                priority))
                            (s/label :text (str "Tags: "
                                                (join ", " tags)))
                            delete-button edit-button])))

(defn draw-collection
  [todos]
  (map draw-todo todos))

(defn show-notification
  [notification]
  (s/alert frame notification))

(defn draw
  [todos]
  (let [items (draw-collection todos)]
    (attach-status-listeners)
    (attach-search-listeners)
    (attach-create-listeners)
    (-> frame
      (s/config! :content (s/config! panel :items (conj items top)))
      (s/show!))))