(ns usermanager.controllers.user
  (:require [ring.util.response :as resp]
            [selmer.parser :as tmpl]
            [next.jdbc.sql :as sql]
            [usermanager.model.user-manager :as model]))

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

(defn get-users
  "Render the list view with all the users in the addressbook."
  [req]
  (let [users (model/get-users model/conn)]
    (-> req
        (assoc-in [:params :users] users)
        (assoc :application/view "list"))))

(defn edit [req]
  (let [db model/conn
        user (when-let [id (get-in req [:params :id])]
                 (model/get-user-by-id db id))]
    (-> req
        (update :params assoc
                :user user
                :departments (model/get-departements db))
        (assoc :application/view "form"))))
