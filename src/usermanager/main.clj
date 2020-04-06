(ns usermanager.main
  (:require [compojure.core :refer [defroutes GET]]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.adapter.jetty :refer [run-jetty]]
            [compojure.route :refer [not-found]]))

(defroutes app-routes
  (GET "/" [] "<h1>OK C'est cool</h1>")
  (GET "/user/list" [] "<p>La liste des utilisateurs</p>")
  (not-found "404: Page not found"))

(def app (wrap-defaults #'app-routes site-defaults))

(defn -main []
  (run-jetty app {:port 3000
                  :join? false}))
