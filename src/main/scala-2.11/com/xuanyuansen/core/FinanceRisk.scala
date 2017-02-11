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
    val mode = args.head

    val basePath = args.apply(1)

    val baseUserInfoPath = basePath + "user_info_%s.txt".format(mode)
    val browsePath = basePath + "browse_history_%s.txt".format(mode)
    val billPath = basePath + "bill_detail_%s.txt".format(mode)
    val bankPath = basePath + "bank_detail_%s.txt".format(mode)
    val loanPath = basePath + "loan_time_%s.txt".format(mode)
    val labelPath = basePath + "overdue_train.txt"

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
          tmp.head -> Seq((tmp.apply(1).toLong, tmp.apply(2).toInt, tmp.apply(3).toInt))
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

    val finalFeature = userFeature
      .fullOuterJoin(
        browseFeature.map { k => k._1 -> k._2.map { m => (m._1 + userSize) -> m._2 } }
      ).map { k => k._1 -> k._2._1.getOrElse(Seq((0, 0.0))).++(k._2._2.getOrElse(Seq((userSize, 0.0)))) }
      .fullOuterJoin(
        billFeature.map { k => k._1 -> k._2.map { m => (m._1 + userSize + browseSize) -> m._2 } }
      ).map { k => k._1 -> k._2._1.getOrElse(Seq((0, 0.0))).++(k._2._2.getOrElse(Seq((userSize + browseSize, 0.0)))) }
      .fullOuterJoin(
        bankFeature.map { k => k._1 -> k._2.map { m => (m._1 + userSize + browseSize + billSize) -> m._2 } }
      ).map { k => k._1 -> k._2._1.getOrElse(Seq((0, 0.0))).++(k._2._2.getOrElse(Seq((userSize + browseSize + billSize, 0.0)))) }

    println("final")
    testRdd(finalFeature)

    if (mode.equals("train")) {
      val label = sc.textFile(labelPath).map {
        r =>
          val tmp = r.split(",")
          tmp.head -> tmp.last.toInt
      }

      val trainSet = label
        .join(finalFeature).map {
          r =>
            r._2._1 + "\t" + r._2._2.map { r => r._1.toString + ":" + r._2.toString }.mkString("\t")
        }

      println("label data size is %d".format(trainSet.count()))
      testRdd(trainSet)

      trainSet.saveAsTextFile(args.apply(2))
    } else {
      finalFeature.map {
        r =>
          r._1 + "\t" + r._2.map { r => r._1.toString + ":" + r._2.toString }.mkString("\t")
      }
        .saveAsTextFile(args.apply(2))
    }

    sc.stop()
    println("work done")
  }

}
