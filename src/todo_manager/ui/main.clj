(ns todo-manager.ui.main
  (:require [seesaw.core :as s]
            [todo-manager.data-handler.writer
             :refer [unparse-time]]
            [todo-manager.data-handler.storage
             :refer [delete-todo add-todo
                     mark-completed reopen set-in-progress]]
            [todo-manager.data-filters.search
             :refer [build-search-criteria search-all]]
            [todo-manager.data-filters.order
             :refer [order-by-priority order-by-progress]]
            [todo-manager.data-handler.validator :refer [validate]]
            [todo-manager.conf :refer [status-mapper
                                       new-todos
                                       todos-in-progress
                                       completed-todos]]
            [clojure.string :refer [join blank?]]))

(declare draw-collection)
(declare draw-todo)
(declare draw-form)
(declare draw-edit-form)
(declare redraw-main-frame)
(declare redraw-panel-items)
(declare remove-listeners)

(def todo (atom {}))

(def frame (s/frame :title "TODO Manager" :height 820 :width 640
                    :resizable? true :visible? true :on-close :exit))

(def button-group (s/button-group))

(def ordering-button-group (s/button-group))

(def statuses (s/flow-panel :items [(s/label :text "Select status")
                                    (s/radio :text "new" :id :new
                                             :group button-group)
                                    (s/radio :text "in-progress" :id :in-progress
                                             :group button-group)
                                    (s/radio :text "completed" :id :completed
                                             :group button-group)
                                    (s/radio :text "all" :id :all :selected? true
                                             :group button-group)]
                           :align :center :hgap 20 :vgap 20))

(def ordering (s/flow-panel :items [(s/label :text "Order by")
                                    (s/radio :text "priority" :id :priority
                                             :selected? true
                                             :group ordering-button-group)
                                    (s/radio :text "progress" :id :progress
                                             :group ordering-button-group)]
                           :align :center :hgap 20 :vgap 20))

(def search-bar (s/horizontal-panel :items
                                    [(s/text  :text "Search your TODOs here!"
                                              :id :input)
                                    (s/button :text "Search" :id :search-button
                                              :halign :center :valign :center)]))

(def add-todo-button (s/button :text "Create TODO"
                        :halign :center
                        :valign :center))

(def header (s/vertical-panel :items
                           [statuses
                            search-bar
                            ordering
                            add-todo-button]))

(def panel (s/flow-panel :items [header]
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

(defn resolve-todos-ordering
  [coll]
  (let [order {:progress order-by-progress
               :priority order-by-priority}
        selected-order (s/id-of (s/selection ordering-button-group))]
    ((selected-order order) coll)))

(def back-button
  (s/button :text "Back" :id :back
            :listen
            [:action
             (fn [e]
               (remove-listeners (s/select panel [:.save]))
               (s/selection! button-group (s/select statuses [:#all]))
               (redraw-main-frame (resolve-todos-type)))]))

(def create-button
  (s/button :text "Create" :id :create))

(defn remove-listeners
  [buttons]
  (doseq [button buttons
          listener (.getActionListeners button)]
    (.removeActionListener button listener)))

(defn attach-ordering-listeners
  []
  (doseq [id [:#priority :#progress]
          :let [selector (s/select ordering [id])]]
    (s/listen selector
              :action
              (fn [e]
                (s/selection! ordering-button-group selector)
                (redraw-panel-items)))))

(defn attach-status-listeners
  []
  (doseq [id [:#new :#in-progress :#completed :#all]
          :let [selector (s/select statuses [id])]]
    (s/listen selector
              :action
              (fn [e]
                (s/selection! button-group selector)
                (redraw-panel-items)))))

(defn attach-search-listeners
  []
  (let [input-selector (s/select search-bar [:#input])
        button-selector (s/select search-bar [:#search-button])]
    (s/listen button-selector
              :action
              (fn [e]
                (s/selection! button-group (s/select statuses [:#all]))
                (s/config! panel :items (-> input-selector
                                            (s/config :text)
                                            (build-search-criteria :or)
                                            (search-all)
                                            vec
                                            (resolve-todos-ordering)
                                            (draw-collection)
                                            (conj header)))))))

(def form-button-group (s/button-group))

(def form-status-buttons [(s/radio :text "new" :id :new
                                   :group form-button-group)
                          (s/radio :text "in-progress" :id :in-progress
                                   :group form-button-group)
                          (s/radio :text "completed" :id :completed
                                   :group form-button-group)])

(defn collect-text
  []
  (doseq [property [:goal, :start_date, :end_date, :priority, :progress, :tags]
                    :let [id (keyword (str "#" (name property)))
                          selector (s/select frame [id])]]
    (swap! todo assoc property (s/config selector :text)))
    (swap! todo assoc :status
                      (s/id-of (s/selection form-button-group))))

(defn validate-before-save
  [& {:keys [with-delete? old-todo]
      :or {with-delete? false old-todo nil}}]
  (collect-text)
  (let [error-message (validate @todo)]
    (if (blank? error-message)
      (do
        (add-todo @todo status-mapper)
        (if with-delete?
          (delete-todo old-todo status-mapper)))
      (s/alert frame error-message))))

(defn attach-todo-form-listeners
  []
  (s/listen create-button
            :action
            (fn [e]
              (validate-before-save))))

(defn attach-create-listener
  []
  (s/listen add-todo-button
            :action
            (fn [e]
              (s/config! frame :content (draw-form)))))

(defn attach-edit-save-listener
  [button old-todo]
  (s/listen button
            :action
            (fn [e]
              (validate-before-save :with-delete? true
                                    :old-todo old-todo))))

(defn attach-todo-listeners
  [delete-button edit-button
   completed open-as-new
   start todo]
  (s/listen delete-button
            :action
            (fn [e]
              (delete-todo todo status-mapper)
              (redraw-panel-items)))
  (s/listen edit-button
            :action
            (fn [e]
              (s/config! panel :items
                               [(draw-edit-form todo) back-button])))
  (s/listen completed
            :action
            (fn [e]
              (mark-completed todo status-mapper)
              (redraw-panel-items)))
  (s/listen open-as-new
            :action
            (fn [e]
              (reopen todo status-mapper)
              (redraw-panel-items)))
  (s/listen start
            :action
            (fn [e]
              (set-in-progress todo status-mapper)
              (redraw-panel-items))))

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
        status (s/flow-panel :items form-status-buttons
                             :align :center
                             :hgap 20 :vgap 20)]

    (s/selection! form-button-group (s/select status [:#new]))
    (s/vertical-panel :id :todo-form
                      :items (-> property-panels
                                 vec
                                 (conj status)
                                 (conj create-button)
                                 (conj back-button)))))

(defn draw-edit-form
  [{status :status start_date :start_date
    end_date :end_date goal :goal
    priority :priority progress :progress
    tags :tags :as todo}]
  (let [properties [:goal :start_date :end_date :progress :priority :tags]
        property-panels (for [property properties
                              :let [text (condp = property
                                            :start_date (unparse-time start_date)
                                            :end_date (unparse-time end_date)
                                            :tags (join ", " tags)
                                            (property todo))]]
                          (s/flow-panel :align :left
                                        :hgap 10
                                        :vgap 20
                                        :items [(s/label :text (name property))
                                                (s/text :text text
                                                        :id property :halign :left
                                                        :columns 40 :margin 15)]))
        status-panel (s/flow-panel :items form-status-buttons
                                   :align :center
                                   :hgap 20 :vgap 20)
        edit-save-button (s/button :text "Save" :class :save)]
    (s/selection! form-button-group
                  (s/select status-panel [(keyword (str "#" (name status)))]))
    (attach-edit-save-listener edit-save-button todo)
    (s/vertical-panel :id :todo-form
                      :items (-> property-panels
                                 vec
                                 (conj status-panel)
                                 (conj edit-save-button)
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
                              :class :edit)
        completed (s/checkbox :text "mark completed"
                              :halign :center
                              :valign :center
                              :class :mark-completed)
        open-as-new (s/checkbox :text "reopen"
                              :halign :center
                              :valign :center
                              :class :reopen)
        start (s/checkbox :text "start"
                          :halign :center
                          :valign :center
                          :class :start)
        shortcuts (s/flow-panel :items [completed open-as-new start]
                                :align :center :hgap 2 :vgap 2)]
    (attach-todo-listeners delete-button edit-button completed open-as-new start todo)
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
                              delete-button edit-button shortcuts])))

(defn draw-collection
  [todos]
  (map draw-todo todos))

(defn show-notification
  [notification]
  (s/alert frame notification))

(defn redraw-panel-items
  []
  (s/config! panel :items (-> (resolve-todos-type)
                              (resolve-todos-ordering)
                              (draw-collection)
                              (conj header))))

(defn redraw-main-frame
  [coll]
  (let [delete-buttons (s/select panel [:.delete])
        edit-buttons (s/select panel [:.edit])
        completed-boxes  (s/select panel [:.mark-completed])
        reopen-boxes (s/select panel [:.reopen])
        start-boxes (s/select panel [:.start])
        buttons (-> []
                    (into delete-buttons)
                    (into edit-buttons)
                    (into completed-boxes)
                    (into reopen-boxes)
                    (into start-boxes))]
    (remove-listeners buttons))

  (let [items (-> coll
                  (resolve-todos-ordering)
                  (draw-collection)
                  (conj header))]
    (->> (s/config! panel :items items)
         (s/config! frame :content)
         (s/show!))))

(defn draw
  [todos]
  (let [items (-> todos
                  (order-by-priority)
                  (draw-collection))]
    (attach-status-listeners)
    (attach-ordering-listeners)
    (attach-search-listeners)
    (attach-create-listener)
    (attach-todo-form-listeners)
    (-> frame
        (s/config! :content (s/config! panel :items (conj items header)))
        (s/show!))))