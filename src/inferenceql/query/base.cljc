(ns inferenceql.query.base
  "Functions for issuing queries. This is not a public API.

  Contains fns shared by both strict and permissive IQL variants."
  (:refer-clojure :exclude [eval])
  (:require #?(:clj [inferenceql.query.command :as command])
            [inferenceql.query.db :as db]
            [inferenceql.query.error :as error]
            [inferenceql.query.plan :as plan]
            [inferenceql.query.relation :as relation]
            [inferenceql.query.statement :as statement]
            [instaparse.core :as insta]))

(defn query
  "Issues a query against a database. Returns a relation or nil. A parsing
  function that turns a string into a parse tree must be provdied as the third
  argument.

  Params:
  - s - a query string
  - db - a database wrapped in an atom/derefable
  - parse - a parsing function"
  [s db parse]
  (let [node-or-failure (parse s)]
    (cond (insta/failure? node-or-failure)
          (throw (error/parse node-or-failure))

          (plan/relation-node? node-or-failure)
          (let [plan (plan/plan node-or-failure)
                env (db/env @db)]
            (tap> #:base.query{:node node-or-failure
                               :plan plan
                               :env env})
            (plan/eval plan env {}))

          (statement/statement-node? node-or-failure)
          (do (statement/execute node-or-failure db)
              nil)

          #?@(:clj [(command/command-node? node-or-failure)
                    (do (command/execute node-or-failure db)
                        nil)]))))

(defn q
  "Returns the result of executing a query on a set of rows. A parsing
  function that turns a string into a parse tree must be provided as the third
  argument.

  Demunges the result's keys and its attributes.

  Params:
  - s - a query string
  - db - a database
  - parse - a parsing function"
  [s db parse]
  (when-let [rel (query s (atom db) parse)]
    (let [stringify-keys #(update-keys % str)
          str-rel (map stringify-keys rel)
          str-attrs (map str (relation/attributes rel))]
      (with-meta str-rel
        {:iql/columns str-attrs}))))
