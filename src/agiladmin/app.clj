;; Copyright (C) 2015-2017 Dyne.org foundation

;; Sourcecode designed, written and maintained by
;; Denis Roio <jaromil@dyne.org>

;; This program is free software: you can redistribute it and/or modify
;; it under the terms of the GNU Affero General Public License as published by
;; the Free Software Foundation, either version 3 of the License, or
;; (at your option) any later version.

;; This program is distributed in the hope that it will be useful,
;; but WITHOUT ANY WARRANTY; without even the implied warranty of
;; MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
;; GNU Affero General Public License for more details.

;; You should have received a copy of the GNU Affero General Public License
;; along with this program.  If not, see <http://www.gnu.org/licenses/>.

(ns ^:skip-aot agiladmin.app
  (:require [agiladmin.handlers :as handler]
			[agiladmin.config :refer :all])
  (:import
   (javafx.beans.value ChangeListener ObservableValue)
   (javafx.concurrent Worker$State)
   (javafx.event ActionEvent EventHandler)
   (javafx.scene Scene)
   (javafx.scene Cursor)
   (javafx.scene.control Button)
   (javafx.scene.layout StackPane)
   (javafx.stage Stage)
   (javafx.scene.web WebView)))

(gen-class
 :name agiladmin.app
 :extends javafx.application.Application
 :prefix "app-")

(defonce ^:private backend (atom 0))

(defn app-start [app ^Stage stage]
 (swap! run-mode :desk)
  (let [server (handler/start-backend)
        root (StackPane.)
        btn (Button.)
        web-view (WebView.)
        scene (Scene. root 800 600)
        state-prop (.stateProperty (.getLoadWorker (.getEngine web-view)))
        url "http://localhost:6060/"]

    ;; Add a WebView (headless browser)
    (.add (.getChildren root) web-view)
    ;; Register listener for WebView state changes
    (.addListener state-prop
                  (proxy [ChangeListener] []
                    (changed [^ObservableValue ov
                              ^Worker$State old-state
                              ^Worker$State new-state]
                      (.setCursor web-view Cursor/WAIT)
;                      (println (str "Current state:" (.name new-state)))
                      (if (= new-state Worker$State/SUCCEEDED)
                          (.setCursor web-view Cursor/DEFAULT))
;                        (println (str "URL '" url "' load completed!")))
                      )))
    ;; Load a URL
    (.load (.getEngine web-view) url)

    ;; Set scene and show stage
    (.setScene stage scene)
    (.show stage)
    (swap! backend (constantly server))))

(defn app-stop [app]
  (handler/stop-backend @backend)
  (swap! backend (constantly nil))
  (println "Quit."))

(defn -main []
  (javafx.application.Application/launch
   agiladmin.app (into-array String [])))
