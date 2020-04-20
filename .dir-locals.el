;;; Directory Local Variables
;;; For more information see (info "(emacs) Directory Variables")

((clojure-mode
  (cider-ns-refresh-before-fn . "integrant.repl/suspend")
  (cider-ns-refresh-after-fn . "integrant.repl/resume")
  (cider-clojure-cli-global-options . "-A:dev")))
