package com.xuanyuansen.core

import com.typesafe.scalalogging.Logger
import org.apache.spark.{ SparkConf, SparkContext }
import org.slf4j.LoggerFactory

/**
 * Created by wangshuai on 2016/12/20.
 * main func
 */

object Rockit {
  @transient lazy private val logger = Logger(LoggerFactory.getLogger(this.getClass))
  private val parser = com.xuanyuansen.conf.ParamParser.getArgParser

  def main(args: Array[String]): Unit = {
    var files: Seq[String] = Seq()
    var algos: Seq[String] = Seq()
    var app: String = ""
    var label: String = ""

    this.parser.parse(args, com.xuanyuansen.conf.AppParam()) match {
      case Some(config) =>
        logger.info(config.algorithms.mkString(","))
        logger.info(config.files.mkString(","))
        logger.info(config.apps)
        files = config.files
        algos = config.algorithms
        app = config.apps
        label = config.label

      case None =>
        logger.error("bad argument, please check your argument!")
        throw new IllegalArgumentException("bad argument, please check your argument!")
    }

    val conf = new SparkConf().setAppName("rock it")
    val sc = new SparkContext(conf)

    val dataRdd = sc.union(files.map(r => sc.textFile(r)))
    val labelRdd = sc.textFile(label)

    sc.stop()
  }
}
