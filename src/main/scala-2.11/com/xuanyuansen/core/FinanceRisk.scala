package com.xuanyuansen.core

import com.xuanyuansen.feature.FeatureTransformer
import org.apache.spark.rdd.RDD
import org.apache.spark.{ SparkConf, SparkContext }

/**
 * Created by wangshuai on 2017/2/10.
 */
object FinanceRisk {
  val userSize = 6
  val bankSize = 15
  val browseSize = 3600
  val billSize = 94

  def testRdd[T](data: RDD[T]): Unit = {
    data.take(10).foreach(println)
  }

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("Finance Risk")
    val sc = new SparkContext(conf)

    val basePath = "file:////home/lianhua/bgdata/fiancedata/train/"

    val baseUserInfoPath = basePath + "user_info_train.txt"
    val browsePath = basePath + "browse_history_train.txt"
    val billPath = basePath + "bill_detail_train.txt"
    val bankPath = basePath + "bank_detail_train.txt"
    val loanPath = basePath + "loan_time_train.txt"

    val loan = sc.textFile(loanPath).map {
      r =>
        val tmp = r.split(",")
        tmp.head -> tmp.last.toLong
    }

    val userFeature = sc
      .textFile(baseUserInfoPath)
      .map(r => FeatureTransformer.fianceProfileFeature(r))
    println("user")
    testRdd(userFeature)

    val browseFeature = sc.
      textFile(browsePath)
      .map {
        r =>
          val tmp = r.split(",")
          tmp.head -> Seq((tmp.apply(1).toLong, tmp.apply(1).toInt, tmp.apply(1).toInt))
      }
      .reduceByKey(_ ++ _)
      .join(loan)
      .map {
        r =>
          r._1 -> r._2._1.filter(k => k._1 <= r._2._2)
      }.map {
        r =>
          r._1 -> FeatureTransformer.browseFeature(r._2)
      }

    println("browse")
    testRdd(browseFeature)

    val billFeature = sc
      .textFile(billPath).map {
        r =>
          val tmp = r.split(",")
          tmp.head -> (tmp.apply(1).toLong, tmp.slice(2, tmp.length))
      }.join(loan)
      .filter(r => r._2._1._1 <= r._2._2).map { r =>
        r._1 -> Seq(r._2._1._2.map { k => k.toDouble })
      }.reduceByKey(_ ++ _)
      .map { k =>
        k._1 -> FeatureTransformer.billFeature(k._2)
      }

    println("bill")
    testRdd(billFeature)

    val bankFeature = sc
      .textFile(bankPath).map {
        r =>
          val tmp = r.split(",")
          tmp.head -> (tmp.apply(1).toLong, tmp.apply(2).toInt, tmp.apply(3).toDouble, tmp.apply(4).toInt)
      }
      .join(loan)
      .filter { r => r._2._1._1 <= r._2._2 }
      .map {
        r =>
          r._1 -> Seq(r._2._1)
      }.reduceByKey(_ ++ _)
      .map { r => r._1 -> FeatureTransformer.bankFeature(r._2) }

    println("bank")
    testRdd(bankFeature)

    sc.stop()
    println("work done")
  }

}
