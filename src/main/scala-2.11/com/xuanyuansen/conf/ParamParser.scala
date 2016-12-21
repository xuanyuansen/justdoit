package com.xuanyuansen.conf

import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory

/**
 * Created by wangshuai on 2016/12/20.
 */
case class AppParam(
  app: String = "",
  debug: Boolean = false,
  files: Seq[String] = Seq(),
  algorithms: Seq[String] = Seq(),
  label: String = "",
  mode : String = "",
  baseLineModelPath : String = ""
)

object ParamParser {
  @transient lazy private val logger = Logger(LoggerFactory.getLogger(this.getClass))

  def getArgParser: scopt.OptionParser[AppParam] = {
    new scopt.OptionParser[AppParam]("Rockit") {
      head("Rockit", "1.0")

      opt[String]('b', "baseline") required () valueName "<baseline>" action {
        (x, c) =>
          c.copy(baseLineModelPath = x)
      }


      opt[String]('m', "mode") required () valueName "<mode>" action {
        (x, c) =>
          c.copy(app = x)
      }


      opt[String]('c', "app") required () valueName "<app>" action {
        (x, c) =>
          c.copy(app = x)
      }

      opt[String]('l', "label") required () valueName "<label>" action {
        (x, c) =>
          c.copy(label = x)
      }

      opt[Seq[String]]('f', "files") required () valueName "<file1>,<file2>" action {
        (x, c) =>
          c.copy(files = x)
      } text "specify files"

      opt[Seq[String]]('a', " algorithms") valueName "<algorithm1>,<algorithm2>" action {
        (x, c) =>
          c.copy(algorithms = x)
      } text "specify the algorithms"

      opt[Unit]("debug") hidden () action { (_, c) =>
        c.copy(debug = true)
      } text "this option is hidden in the usage text"

      note("some notes.\n")
      help("help") text "prints this usage text"
    }
  }
}