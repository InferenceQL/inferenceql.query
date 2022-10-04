(ns inferenceql.query.xforms
  (:require [inferenceql.query.math :as math]))

(defn median
  [xf]
  (let [coll (volatile! [])]
    (fn
      ([]
       (xf))
      ([acc]
       (xf acc (math/median @coll)))
      ([acc input]
       (vswap! coll conj input)
       acc))))

(defn sum
  [xf]
  (let [coll (volatile! [])]
    (fn
      ([]
       (xf))
      ([acc]
       (xf acc (reduce + 0 @coll)))
      ([acc input]
       (vswap! coll conj input)
       acc))))
