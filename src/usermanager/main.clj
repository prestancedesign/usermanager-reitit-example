(ns usermanager.main
  (:require [integrant.core :as ig]
            [reitit.ring :as ring]
            [reitit.ring.middleware.parameters :as parameters]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.util.response :as resp]
            [usermanager.controllers.user :as user-ctl]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]))

(defn my-middleware
  "This middleware runs for every request and can execute before/after logic.

  If the handler returns an HTTP response (like a redirect), we're done.
  Else we use the result of the handler to render an HTML page."
  [handler]
  (fn [req]
    (let [resp (handler req)]
      (if (resp/response? resp)
        resp
        (user-ctl/render-page resp)))))

(def app
  (ring/ring-handler
   (ring/router
    [["/" {:handler #'user-ctl/default}]
     ["/reset" {:handler #'user-ctl/reset-changes}]
     ["/user"
      ["/list" {:handler #'user-ctl/get-users}]
      ["/form" {:handler #'user-ctl/edit}]
      ["/form/:id" {:get {:parameters {:path {:id int?}}}
                    :handler #'user-ctl/edit}]
      ["/save" {:post {:handler #'user-ctl/save}}]
      ["/delete/:id" {:get {:parameters {:path {:id int?}}}
                      :handler #'user-ctl/delete-by-id}]]]
    {:data {:middleware [my-middleware
                         parameters/parameters-middleware
                         wrap-keyword-params]}})
   (ring/routes
    (ring/create-resource-handler
     {:path "/"})
    (ring/create-default-handler
     {:not-found (constantly {:status 404 :body "Not found"})}))))

(def config
  {:adapter/jetty {:port 3000 :handler (ig/ref :handler/run-app)}
   :handler/run-app app})

(defmethod ig/init-key :adapter/jetty [_ {:keys [handler] :as opts}]
  (run-jetty handler (-> opts (dissoc handler) (assoc :join? false))))

(defmethod ig/halt-key! :adapter/jetty [_ server]
  (.stop server))

(defmethod ig/init-key :handler/run-app [_ _]
  app)

(def system
  (ig/init config))

(defn -main []
  system)
