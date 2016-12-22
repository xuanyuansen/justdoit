package com.xuanyuansen.application

import com.xuanyuansen.core.BaseAlgorithmPipeline
import org.apache.spark.{ SparkConf, SparkContext }

/**
 * Created by wangshuai on 2016/12/20.
 * add object to do sth.
 */
class overdue extends BaseAlgorithmPipeline[String, Double] {

}

object overdue {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("rock it")
    val sc = new SparkContext(conf)

    if (args.length==4) {

      val file = "file:////home/lianhua/bgdata/fiancedata/train/browse_history_train.txt"
      val label = "file:////home/lianhua/bgdata/fiancedata/train/overdue_train.txt"
      val trainFile = "file:////home/lianhua/bgdata/fiancedata/train/sequence_label_to_train.txt"
      val dictFile = "file:////home/lianhua/bgdata/fiancedata/train/dict.txt"

      val sequenceData = sc
        .textFile(file, 30)
        .map {
          r =>
            val tmp = r.split(",")
            tmp.head -> Seq((tmp.apply(1).toLong, "%s_%s".format(tmp.apply(2), tmp.apply(3))))
        }.reduceByKey(_ ++ _)
        .map {
          r =>
            r._1 -> r._2.sortWith((x, y) => if (x._1 < y._1) true else false)
        }

      sequenceData.take(10).foreach(println)

      val dictRdd = sequenceData
        .flatMap(r => r._2)
        .map { r => r._2 }
        .distinct(numPartitions = 1)

      dictRdd.saveAsTextFile(dictFile)

      val labelFile = sc
        .textFile(label, 20).map {
        r =>
          val tmp = r.split(",")
          tmp.head -> tmp.apply(1)
      }

      sequenceData
        .join(labelFile)
        .map { r =>
          r._2._2 + "\t" + r._2._1.map(_._2).mkString(",")
        }
        .saveAsTextFile(trainFile)
    }
    else if (args.length==2){
      val sequenceData = sc
        .textFile(args.head, 30)
        .map {
          r =>
            val tmp = r.split(",")
            tmp.head -> Seq((tmp.apply(1).toLong, "%s_%s".format(tmp.apply(2), tmp.apply(3))))
        }.reduceByKey(_ ++ _)
        .map {
          r =>
            r._1 -> r._2.sortWith((x, y) => if (x._1 < y._1) true else false)
        }

      sequenceData
        .map { r =>
          r._1 + "\t" + r._2.map{k=>k._2}.mkString(",")
        }
        .saveAsTextFile(args.apply(1))
    }
    sc.stop()
  }

}
