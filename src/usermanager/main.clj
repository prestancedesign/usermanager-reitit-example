(ns usermanager.main
  (:require [compojure.core :refer [defroutes GET]]
            [compojure.route :refer [not-found resources]]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.defaults :refer [site-defaults wrap-defaults]]
            [ring.util.response :as resp]
            [usermanager.controllers.user :as user-ctl]))

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

(defroutes app-routes
  (GET "/" [] (my-middleware #'user-ctl/default))
  (GET "/user/list" [] "<p>La liste des utilisateurs</p>")
  (resources "/")
  (not-found "404: Page not found"))

(def app (wrap-defaults #'app-routes site-defaults))

(defn -main []
  (run-jetty #'app-routes {:port 3000
                           :join? false}))
