(ns usermanager.controllers.user
  (:require [ring.util.response :as resp]
            [selmer.parser :as tmpl]))

(def ^:private changes (atom 0))

(defn render-page [req]
  (let [data (assoc (:params req) :changes @changes)
        view (:application/view req "default")
        html (tmpl/render-file (str "views/user/" view ".html") data)]
    (-> (resp/response (tmpl/render-file (str "layouts/default.html")
                                         (assoc data :body [:safe html])))
        (resp/content-type "text/html"))))

(defn default [req]
  (assoc-in req [:params :message]
            (str "Bienvenu sur la démo du gestionnaire d'utilisateurs!"
                 "Ce site utilise simplement Compojure, Ring et Selmer")))
