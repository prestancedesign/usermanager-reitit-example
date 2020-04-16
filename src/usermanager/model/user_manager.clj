(ns usermanager.model.user-manager
  (:require [mount.core :as mount :refer [defstate]]
            [next.jdbc :as jdbc]
            [next.jdbc.sql :as sql]))

(def ^:private my-db
  "SQLite database connection spec."
  {:dbtype "sqlite" :dbname "usermanager_db"})

(def ^:private departments
  "List of departments."
  ["Accounting" "Sales" "Support" "Development"])

(def ^:private initial-user-data
  "Seed the database with this data."
  [{:first_name "Michael" :last_name "Salihi"
    :email "admin@prestance-informatique.fr" :department_id 4}])

(defn- populate
  "Called at application startup. Attempts to create the
  database table and populate it. Takes no action if the
  database table already exists."
  [db db-type]
  (let [auto-key (if (= "sqlite" db-type)
                   "primary key autoincrement"
                   (str "generated always as identity"
                        " (start with 1, increment by 1)"
                        " primary key"))]
    (try
      (jdbc/execute-one! db
                         [(str "
create table department (
  id            integer " auto-key ",
  name          varchar(32)
)")])
      (jdbc/execute-one! db
                         [(str "
create table addressbook (
  id            integer " auto-key ",
  first_name    varchar(32),
  last_name     varchar(32),
  email         varchar(64),
  department_id integer not null
)")])
      (println "Created database and addressbook table!")
      ;; if table creation was successful, it didn't exist before
      ;; so populate it...
      (try
        (doseq [d departments]
          (sql/insert! db :department {:name d}))
        (doseq [row initial-user-data]
          (sql/insert! db :addressbook row))
        (println "Populated database with initial data!")
        (catch Exception e
          (println "Exception:" (ex-message e))
          (println "Unable to populate the initial data -- proceed with caution!")))
      (catch Exception e
        (println "Exception:" (ex-message e))
        (println "Looks like the database is already setup?")))))

(defn- start-db [db-spec]
  (let [conn (jdbc/get-datasource db-spec)]
    (populate conn (:dbtype db-spec))
    conn))

(defstate conn :start (start-db my-db))

(defn get-users
  "Return all available users, sorted by name.

  Since this is a join, the keys in the hash maps returned will
  be namespace-qualified by the table from which they are drawn:

  addressbook/id, addressbook/first_name, etc, department/name"
  [db]
  (sql/query db
             ["
select a.*, d.name
 from addressbook a
 join department d on a.department_id = d.id
 order by a.last_name, a.first_name
"]))

(defn get-user-by-id [db id]
  (sql/get-by-id db :addressbook id))

(defn get-departements [db]
  (sql/query db ["select * from department order by name"]))

(defn save-user [db user]
  (let [id (:addressbook/id user)]
    (if (and id (not (zero? id)))
      (sql/update! db :addressbook
                   (dissoc user :addressbook/id)
                   {:id id})
      (sql/insert! db :addressbook
                   (dissoc user :addressbook/id)))))

(defn delete-user-by-id [db id]
  (sql/delete! db :addressbook {:id id}))
