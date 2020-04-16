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

(defn reset-changes
  [req]
  (reset! changes 0)
  (assoc-in req [:params :message] "The change tracker has been reset."))

(defn default [req]
  (assoc-in req [:params :message]
            (str "Welcome to the User Manager application demo! "
                 "This uses just Reitit, Ring, and Selmer.")))

(defn get-users
  "Render the list view with all the users in the addressbook."
  [req]
  (let [users (model/get-users model/conn)]
    (-> req
        (assoc-in [:params :users] users)
        (assoc :application/view "list"))))

(defn edit [req]
  (let [db model/conn
        user (when-let [id (get-in req [:path-params :id])]
               (model/get-user-by-id db id))]
    (-> req
        (update :params assoc
                :user user
                :departments (model/get-departements db))
        (assoc :application/view "form"))))

(defn save [req]
  (swap! changes inc)
  (-> req
      :params
      (select-keys [:id :first_name :last_name :email :department_id])
      (update :id #(some-> % not-empty Long/parseLong))
      (update :department_id #(some-> % not-empty Long/parseLong))
      (->> (reduce-kv (fn [m k v] (assoc! m (keyword "addressbook" (name k)) v))
                      (transient {}))
           (persistent!)
           (model/save-user model/conn)))
  (resp/redirect "/user/list"))

(defn delete-by-id [req]
  (swap! changes inc)
  (model/delete-user-by-id model/conn
                           (get-in req [:path-params :id]))
  (resp/redirect "/user/list"))
