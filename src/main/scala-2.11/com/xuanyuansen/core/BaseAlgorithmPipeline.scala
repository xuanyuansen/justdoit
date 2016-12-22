package com.xuanyuansen.core

import com.typesafe.scalalogging.Logger
import org.apache.spark.SparkContext
import org.apache.spark.ml.linalg.SparseVector
import org.apache.spark.rdd.RDD
import org.slf4j.LoggerFactory

import scala.reflect.ClassTag

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

class BaseAlgorithmPipeline[T, K] {
  @transient lazy protected val logger = Logger(LoggerFactory.getLogger(this.getClass))

  class MlModel(mType: String, algorithmType: String) {

    def fit(data: RDD[(T, SparseVector)], label: RDD[(T, K)]): Unit = {

    }

  }

  def SaveModel(path: String, mType: String, model: MlModel): Unit = {

  }

  def LoadModel(path: String, mType: String): MlModel = {
    null
  }

  def Data2Label(labelFile: String): RDD[(T, K)] = {
    null
  }

  def Data2Feature(dataFile: Seq[String]): RDD[(T, SparseVector)] = {
    null
  }

  def BaseLineTrain(sc: SparkContext, mType: String, algorithmType: String, files: Seq[String], label: String): MlModel = {
    val model = new MlModel(mType, algorithmType)
    val features = Data2Feature(files)
    val labels = Data2Label(label)
    model.fit(features, labels)

    model
  }

  def GridSearchTrain(mTypes: String, algorithmTypes: Seq[String], parameters: Some[String]): Unit = {

  }

  def test(): Unit = {

  }

}
