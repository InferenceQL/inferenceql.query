(ns build
  (:require [clojure.tools.build.api :as build]))

(def lib 'io.github.OpenGen/GenSQL.query)
(def version (format "1.2.%s" (build/git-count-revs nil)))
(def class-dir "target/classes")
(def uber-file (format "target/%s-%s-standalone.jar" (name lib) version))

;; delay to defer side effects (artifact downloads)
(def basis (delay (build/create-basis {:aliases [ :query-sppl ]})))

;; clean build artifacts (excludes test artifacts)
(defn clean [_]
  (build/delete {:path "target"}))

(defn uber [_]
  (clean nil)
  (build/copy-dir {:src-dirs ["src" "resources"]
                   :target-dir class-dir})
  (build/compile-clj {:basis @basis
                      :ns-compile '[gensql.query.main]
                      :class-dir class-dir})
  (build/uber {:class-dir class-dir
               :uber-file uber-file
               :basis @basis
               :main 'gensql.query.main}))
