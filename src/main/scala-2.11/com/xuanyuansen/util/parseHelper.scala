package com.xuanyuansen.util

/**
 * Created by wangshuai on 2017/2/10.
 */
object parseHelper {
  def reduceByKey[K, V](collection: Traversable[Tuple2[K, V]])(implicit num: Numeric[V]) = {
    import num._
    collection
      .groupBy(_._1)
      .map { case (group: K, traversable) => traversable.reduce { (a, b) => (a._1, a._2 + b._2) } }
  }
}
