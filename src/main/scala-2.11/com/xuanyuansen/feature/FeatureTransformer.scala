package com.xuanyuansen.feature

import com.xuanyuansen.util.parseHelper
import org.apache.spark.SparkContext
import org.apache.spark.mllib.feature.{ Word2Vec, Word2VecModel }
import org.apache.spark.rdd.RDD

import scala.collection.mutable

/**
 * Created by wangshuai on 2016/12/20.
 * my feature transformer
 */
object FeatureTransformer {
  /*
  6维
   */
  def fianceProfileFeature(data: String): (String, Seq[(Int, Double)]) = {
    val tmp = data.split(",")
    if (tmp.length != 6)
      null
    else {
      tmp.head -> tmp.slice(1, tmp.length).zipWithIndex.map { r => r._2 -> r._2.toDouble }
    }
  }

  /*
  1维
   */
  /*
  用户id,时间戳,交易类型,交易金额,工资收入标记
  6951,5894316387,0,13.756664,0
   */
  /*
  0、总金额
  1、工资收入次数
  2、均值
  3、方差
  4、标准差
  0类型 总金额，次数，均值，方差，标准差
  1类型 总金额，次数，均值，方差，标准差
   */

  def getAVS(data: Seq[Double]): (Double, Double, Double) = {
    val p_cnt = data.length
    val p_average = data.sum / p_cnt
    val p_variance = data.map { r => (r - p_average) * (r - p_average) }.sum / p_cnt

    (p_average, p_variance, scala.math.sqrt(p_variance))
  }

  def priceFeature(data: Seq[(Long, Int, Double, Int)]): Seq[(Int, Double)] = {
    val paied = data.filter(_._4 == 1).map(_._3)
    val p_avs = getAVS(paied)

    val input = data.filter(r => r._4 == 0 && r._2 == 0).map(_._3)
    val in_avs = getAVS(input)

    val output = data.filter(r => r._4 == 0 && r._2 == 1).map(_._3)
    val out_avs = getAVS(output)

    Seq(
      (0, paied.sum),
      (1, paied.length),
      (2, p_avs._1),
      (3, p_avs._2),
      (4, p_avs._3),
      (5, input.sum),
      (6, input.length),
      (7, in_avs._1),
      (8, in_avs._2),
      (9, in_avs._3),
      (10, output.sum),
      (11, output.length),
      (12, out_avs._1),
      (13, out_avs._2),
      (14, out_avs._3)
    )
  }

  /*
  用户浏览行为browse_history.txt。共4个字段。其中，第2个字段，时间戳为0表示时间未知。
  用户id,时间戳,浏览行为数据,浏览子行为编号
  300*12=360
  base_type = 0-299
  sub_type = 0-11
  base_type * sub_type
  34801,5926003545,82,1
  34801,5926003545,101,1
  */
  def browsFeature(data: Seq[(Long, Int, Int)]): Seq[(Int, Double)] = {
    val info = data.map {
      r =>
        r._2 * r._3 -> 1
    }

    val out = parseHelper.reduceByKey(info)

    out
      .map { r => r._1 -> r._2.toDouble }
      .toSeq
      .sortWith((x, y) => if (x._1 <= y._1) true else false)
  }

  /**
   * size 89
   * 用户id,
   * 账单时间戳,
   * 银行id,
   * 上期账单金额,   1
   * 上期还款金额,   2
   * 信用卡额度,      3
   * 本期账单余额,     4
   * 本期账单最低还款额,  5
   * 消费笔数,       6
   * 本期账单金额,     7
   * 调整金额,       8
   * 循环利息,       9
   * 可用金额,       10
   * 预借现金额度,     11
   * 还款状态
   * 3147,5906744363,6,18.626118,18.661937,20.664418,18.905766,17.847133,1,0.000000,0.000000,0.000000,0.000000,19.971271,0
   * 22717,5934018585,3,0.000000,0.000000,20.233635,18.574069,18.396785,0,0.000000,0.000000,0.000000,0.000000,0.000000,0
   */
  def bankFeature(data: Seq[Array[Double]]): Seq[(Int, Double)] = {
    val outFeature = mutable.ArrayBuffer[(Int, Double)]()
    //1
    outFeature += 0 -> data.length.toDouble

    val fData1 = data.filter { r => r.last == 1 }
    val fData2 = data.filter { r => r.last == 0 }
    //44
    for (i <- 0 to 11) {
      val tmp = fData1.map(r => r.apply(i))
      val tmp_avs = getAVS(tmp)

      outFeature += (4 * i + 1) -> tmp.sum
      outFeature += (4 * i + 2) -> tmp_avs._1
      outFeature += (4 * i + 3) -> tmp_avs._2
      outFeature += (4 * i + 4) -> tmp_avs._3
    }
    //44
    for (i <- 0 to 11) {
      val tmp = fData2.map(r => r.apply(i))
      val tmp_avs = getAVS(tmp)

      outFeature += (4 * (i + 11) + 1) -> tmp.sum
      outFeature += (4 * (i + 11) + 2) -> tmp_avs._1
      outFeature += (4 * (i + 11) + 3) -> tmp_avs._2
      outFeature += (4 * (i + 11) + 4) -> tmp_avs._3
    }

    outFeature
  }

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

  def main(args: Array[String]): Unit = {
    //test1
    val info = "6346,1,2,4,4,2"
    val feature = fianceProfileFeature(info)
    println(info)

    val bankInfo = Seq((5894316387L, 0, 13.756664, 0), (5894321388L, 1, 13.756664, 0))
    val f2 = priceFeature(bankInfo)
    println(f2)

    val browseInfo = Seq((5926003545L, 173, 1), (5926003545L, 164, 4))
    val f3 = browsFeature(browseInfo)
    println(f3)

    val bill1 = Array(6.0, 18.626118, 18.661937, 20.664418, 18.905766, 17.847133, 1.0, 0.000000, 0.000000, 0.000000, 0.000000, 19.971271, 0.0)
    val bill2 = Array(6.0, 18.905766, 18.909954, 20.664418, 19.113305, 17.911506, 1.0, 0.000000, 0.000000, 0.000000, 0.000000, 19.971271, 0.0)
    val billInfo = Seq(bill1, bill2)
    val f4 = bankFeature(billInfo)
    println(f4)
  }
}
