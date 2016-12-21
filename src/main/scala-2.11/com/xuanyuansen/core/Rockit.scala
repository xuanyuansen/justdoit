package com.xuanyuansen.core

import com.typesafe.scalalogging.Logger
import com.xuanyuansen.conf.AppConfig
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
    var mtype: String = ""
    var bpath: String = ""

    this.parser.parse(args, com.xuanyuansen.conf.AppParam()) match {
      case Some(config) =>
        logger.info(config.algorithms.mkString(","))
        logger.info(config.files.mkString(","))
        logger.info(config.app)
        logger.info(config.mode)
        files = config.files
        algos = config.algorithms
        app = config.app
        label = config.label
        mtype = config.mode
        bpath = config.baseLineModelPath

      case None =>
        logger.error("bad argument, please check your argument!")
        throw new IllegalArgumentException("bad argument, please check your argument!")
    }

    val conf = new SparkConf().setAppName("rock it")
    val sc = new SparkContext(conf)

    AppConfig.AppConfig.filter(r => r.appName.equals(app)).foreach {
      r =>
        val strategy = r.strategy
        val baseLineModel = strategy
          .BaseLineTrain(sc = sc, mType = mtype, algorithmType = algos.head, files = files, label = label)
        strategy
          .SaveModel(path = bpath, mType = mtype, model = baseLineModel)
    }

    sc.stop()
  }
}
