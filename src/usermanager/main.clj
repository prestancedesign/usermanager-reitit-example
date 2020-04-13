(ns usermanager.main
  (:require [compojure.core :refer [defroutes GET POST]]
            [compojure.coercions :refer [as-int]]
            [compojure.route :refer [not-found resources]]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.defaults :refer [site-defaults wrap-defaults]]
            [ring.util.response :as resp]
            [usermanager.controllers.user :as user-ctl]
            [mount.core :as mount :refer [defstate]]))

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
  (GET "/user/list" [] (my-middleware #'user-ctl/get-users))
  (GET "/user/form" [] (my-middleware #'user-ctl/edit))
  (GET "/user/form/:id{[0-9]+}" [id :<< as-int] (my-middleware #'user-ctl/edit))
  (POST "/user/save" [] (my-middleware #'user-ctl/save))
  (resources "/")
  (not-found "Error 404: Page not found"))

(def app (wrap-defaults #'app-routes (assoc-in site-defaults [:security :anti-forgery] false)))

(defn- start-server []
  (run-jetty #'app {:port 3000
                    :join? false}))

(defstate server :start (start-server)
                 :stop (.stop server))

(defn -main []
  (mount/start))
