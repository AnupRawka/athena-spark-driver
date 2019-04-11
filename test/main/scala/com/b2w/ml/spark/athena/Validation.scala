package main.scala.com.b2w.ml.spark.athena

import org.apache.spark.sql.SparkSession
import com.b2w.ml.spark.athena._
import org.scalatest._

class Validation extends FlatSpec with Matchers {

  val spark = SparkSession.builder.appName("teste").master("local[*]").getOrCreate

  val freteCalculoQuery =
    """
      |SELECT
      | produto.id
      |FROM
      | frete.calculo
      |LIMIT 10
      |"""
      .stripMargin

  "Athena reader" should "return 10 elements from frete.calculo" in {
    spark
      .read
      .athena(freteCalculoQuery)
      .count() shouldBe 10
  }

}
