package com.xuanyuansen.core

import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory

/**
 * Created by wangshuai on 2016/12/20.
 * base class of ml, basically there are three modes
 * 1. origin spark algorithm, origin
 * 2. scikit-learn on spark, scikit
 * 3. xgboost on spark, xgboost
 *
 * basic algorithms, rf, gbdt
 * deep algorithms, cnn, lstm
 * if graph exists, then using line to embedding the graph
 * if sequence exits, then using word2vec
 */
class BaseAlgorithmPipeline {
  @transient lazy protected val logger = Logger(LoggerFactory.getLogger(this.getClass))

  def SaveModel(path: String, mType: String): Unit = {

  }

  def LoadModel(path: String, mType: String): Unit = {

  }

  def Data2Feature(): Unit = {

  }

  def BaseLineTrain(mType: String, algorithmType: String): Unit = {

  }

  def GridSearchTrain(mTypes: String, algorithmTypes: Seq[String], parameters: Some[String]): Unit = {

  }

  def test(): Unit = {

  }

}
