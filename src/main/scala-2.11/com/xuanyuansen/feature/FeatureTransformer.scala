package com.xuanyuansen.feature

import org.apache.spark.SparkContext
import org.apache.spark.mllib.feature.{ Word2Vec, Word2VecModel }
import org.apache.spark.rdd.RDD

/**
 * Created by wangshuai on 2016/12/20.
 * my feature transformer
 */
object FeatureTransformer {

  def Word2Vec(data: RDD[Seq[String]], minCount: Int = 5, vectorSize: Int = 100, windowSize: Int = 5, seed: Long = 7L, partition: Int = 200): Word2VecModel = {
    val word2vec = new Word2Vec()

    word2vec.setMinCount(minCount)
    word2vec.setVectorSize(vectorSize)
    word2vec.setWindowSize(windowSize)
    word2vec.setSeed(seed)
    word2vec.setNumPartitions(partition)

    word2vec.fit(data)
  }

  def DataToSeqAndDict[T](sc: SparkContext, rDD: RDD[(String, Seq[(T, String)])], minCount: Int = 5, unknown: String = "unknown")(implicit order: T => Ordered[T]): (RDD[(String, Seq[String])], RDD[(String, Int)]) = {
    val seqData = rDD.map {
      r =>
        r._1 -> r._2.sortWith((x, y) => if (x._1 > y._1) true else false).map(_._2)
    }

    val Dict = rDD.flatMap {
      r =>
        r._2.map { _._2 -> 1 }
    }.reduceByKey(_ + _)
      .filter(_._2 >= minCount)
      .union(sc.parallelize(Seq(unknown -> 1)))

    (seqData, Dict)
  }
}
