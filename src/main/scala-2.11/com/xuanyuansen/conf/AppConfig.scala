package com.xuanyuansen.conf

import com.xuanyuansen.application.overdue
import com.xuanyuansen.core.BaseAlgorithmPipeline

/**
 * Created by wangshuai on 2016/12/20.
 */
case class AlgoStrategy(
  appName: String,
  strategy: BaseAlgorithmPipeline
)
    extends Serializable

object AppConfig {
  val AppConfig = Seq(
    AlgoStrategy("overdue", new overdue())
  )
}
