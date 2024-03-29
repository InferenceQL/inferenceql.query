(ns inferenceql.query.permissive.parser-test
  (:require [clojure.test :refer [are deftest]]
            [inferenceql.query.permissive.parser :as parser]
            [instaparse.core :as insta]))

(deftest select-valid
  (are [s] (not (insta/failure? (parser/parse s)))
    "SELECT * FROM data"
    "SELECT x FROM data"
    "SELECT (x) FROM data"
    "SELECT (PROBABILITY OF x UNDER model) FROM data"))

(deftest given-valid
  (are [s] (not (insta/failure? (parser/parse s :start :given-expr)))
    "model GIVEN x = 0"
    "model GIVEN x = y"
    "model GIVEN x > 0"
    "model GIVEN x > y"
    "model GIVEN x = 0 AND y = 0"
    "model GIVEN x = 0 AND y > 0"
    "model GIVEN x > 0 AND y = 0"
    "model GIVEN x = 0, y = 0"
    "model GIVEN x = 0, y > 0"
    "model GIVEN x > 0, y = 0"))

(deftest given-invalid
  (are [s] (insta/failure? (parser/parse s :start :given-expr))
    "model GIVEN VAR x = 0 OR VAR y = 0"
    "model GIVEN VAR x = 0 OR VAR y > 0"
    "model GIVEN VAR x > 0 OR VAR y = 0"))

(deftest generative-join-valid
  (are [s] (not (insta/failure? (parser/parse s)))
    "data GENERATIVE JOIN model"
    "data GENERATIVE JOIN model CONDITIONED BY x = 0"
    "data GENERATIVE JOIN model CONSTRAINED BY x > 0"
    "data GENERATIVE JOIN model GIVEN x = 0"))
