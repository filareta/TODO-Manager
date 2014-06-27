(defproject todo-manager "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [clj-time "0.7.0"]
                 [org.apache.commons/commons-daemon "1.0.9"]]
  :main todo-manager.core
  :aot :all)
