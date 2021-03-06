(ns re-frame.registrar
  (:require  [re-frame.interop :refer [debug-enabled?]]
             [re-frame.loggers    :refer [console]]))


;; kinds of handlers
(def kinds #{:event :fx :sub})

;; This atom contains a register of all handlers.
;; Is a map keyed first by kind (of handler), and then id.
;; leaf nodes are handlers.
(def ^:private kind->id->handler  (atom {}))


(defn get-handler

  ([kind id]
   (-> (get @kind->id->handler kind)
       (get id)))

  ([kind id required?]
   (let [handler (get-handler kind id)]
     (when debug-enabled?
       (when (and required? (nil? handler))
         (console :error "re-frame: no " (str kind) " handler registered for: " id)))
     handler)))


(defn register-handler
  [kind id handler-fn]
  (when debug-enabled?
    (when (get-handler kind id false)
      (console :warn "re-frame: overwriting " (str kind) " handler for: " id)))   ;; allow it, but warn. Happens on figwheel reloads.
  (swap! kind->id->handler assoc-in [kind id] handler-fn)
  handler-fn)    ;; note: returns the just registered handler


(defn clear-handlers
  ([]            ;; clear all kinds
   (reset! kind->id->handler {}))

  ([kind]        ;; clear all handlers for this kind
   (assert (kinds kind))
   (swap! kind->id->handler dissoc kind))

  ([kind id]     ;; clear a single handler for a kind
   (assert (kinds kind))
   (if (get-handler kind id)
     (swap! kind->id->handler update-in [kind] dissoc id)
     (console :warn "re-frame: can't clear " (str kind) " handler for  " id ".  Not found."))))
