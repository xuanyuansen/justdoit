package com.xuanyuansen.util

import ml.dmlc.xgboost4j.scala.DMatrix
import ml.dmlc.xgboost4j.scala.XGBoost

/**
 * Created by wangshuai on 2016/12/20.
 */
object example {
  def main(args: Array[String]): Unit = {
    // read trainining data, available at xgboost/demo/data
    val trainData =
      new DMatrix(args.head)
    // define parameters
    val paramMap = List(
      "eta" -> 0.1,
      "max_depth" -> 2,
      "objective" -> "binary:logistic"
    ).toMap
    // number of iterations
    val round = 2
    // train the model
    val model = XGBoost.train(trainData, paramMap, round)
    // run prediction
    val predTrain = model.predict(trainData)
    // save model to the file.
    model.saveModel(args.apply(1))

  }

}
