(ns inferenceql.query.scalar-test
  (:refer-clojure :exclude [eval])
  (:require [clojure.test :refer [are deftest]]
            [inferenceql.query.parser :as parser]
            [inferenceql.query.scalar :as scalar]
            [inferenceql.query.tuple :as tuple]))

(deftest expr->sexpr
  (are [expr sexpr] (= sexpr (scalar/plan (parser/parse expr :start :scalar-expr)))
    "x"              'x
    "not x"          '(not x)

    "x > y"          '(> x y)
    "x > y > z"      '(> (> x y) z)

    "x = 0"          '(= x 0)
    "x = y = 0"      '(= (= x y) 0)

    "x and y or z"   '(or (and x y) z)
    "x or y and z"   '(or x (and y z))
    "x and (y or z)" '(and x (or y z))
    "(x or y) and z" '(and (or x y) z)

    "x * y + z"      '(+ (* x y) z)
    "x + y * z"      '(+ x (* y z))
    "x * (y + z)"    '(* x (+ y z))
    "(x + y) * z"    '(* (+ x y) z)))

(deftest eval
  (are [x s env tuple attrs] (= x (try (-> (parser/parse s :start :scalar-expr)
                                           (scalar/plan)
                                           (scalar/eval env (tuple/tuple tuple attrs)))
                                       (catch Exception _
                                         :error)))
    false   "NOT true"         '{}      '{}      '[]
    true    "NOT false"        '{}      '{}      '[]

    true    "0 IS 0"           '{}      '{}      '[]
    false   "0 IS 1"           '{}      '{}      '[]
    false   "1 IS 0"           '{}      '{}      '[]
    true    "x IS 0"           '{x 0}   '{}      '[]
    false   "x IS 0"           '{x 1}   '{}      '[]
    true    "0 IS x"           '{x 0}   '{}      '[]
    false   "0 IS x"           '{x 1}   '{}      '[]

    false   "0 IS NOT 0"       '{}      '{}      '[]
    true    "0 IS NOT 1"       '{}      '{}      '[]
    true    "1 IS NOT 0"       '{}      '{}      '[]
    false   "x IS NOT 0"       '{x 0}   '{}      '[]
    true    "x IS NOT 0"       '{x 1}   '{}      '[]
    false   "0 IS NOT x"       '{x 0}   '{}      '[]
    true    "0 IS NOT x"       '{x 1}   '{}      '[]

    true    "NULL IS NULL"     '{}      '{}      '[]
    false   "NULL IS NOT NULL" '{}      '{}      '[]
    false   "0 IS NULL"        '{}      '{}      '[]
    true    "0 IS NOT NULL"    '{}      '{}      '[]
    true    "x IS NULL"        '{x nil} '{}      '[]
    true    "x IS NULL"        '{x nil} '{}      '[x]
    true    "x IS NULL"        '{}      '{x nil} '[x]
    :error  "x IS NULL"        '{}      '{}      '[]
    false   "x IS NOT NULL"    '{x nil} '{}      '[]
    false   "x IS NOT NULL"    '{x nil} '{}      '[]
    false   "x IS NULL"        '{x 0}   '{}      '[]
    true    "x IS NOT NULL"    '{x 0}   '{}      '[]

    1       "x + 1"            '{}      '{x 0}   '[x]
    nil     "x + 1"            '{}      '{}      '[x]
    :error  "x + 1"            '{}      '{}      '[]))
