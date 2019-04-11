package com.b2w.ml.spark

import java.util.Properties

import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClientBuilder
import com.simba.athena.amazonaws.regions.{Region, Regions}
import org.apache.spark.sql.{DataFrame, DataFrameReader}

import scala.collection.JavaConverters._
import scala.util.{Failure, Success, Try}

package object athena {

  implicit class AthenaDataFrameReader(reader: DataFrameReader) {

    def athena(query: String): DataFrame = {

      athena(query, getCurrentRegion)
    }

    def athena(query: String, region:Region): DataFrame = {
      athena(query, getS3StagingDir(), region)
    }

    def athena(query: String, s3StatingDir:String, region:Region = Region.getRegion(Regions.US_EAST_1)): DataFrame = {
      reader.format("com.b2w.ml.spark.athena")
        .option(JDBCOptions.JDBC_TABLE_NAME, s"($query)")
        .option("region", region.getName)
        .option("s3_staging_dir", s3StatingDir)
        .load
    }

    def athena(query: String, properties: Properties): DataFrame = {
      val options = properties.asScala
      options += (JDBCOptions.JDBC_TABLE_NAME -> query)
      reader.format("com.b2w.ml.spark.athena").options(options).load
    }

    def getCurrentRegion:Region = {
      val region = Regions.getCurrentRegion
      if(region  == null)
        Region.getRegion(Regions.US_EAST_1)
      else
        region
    }

    def getS3StagingDir(region:Region = getCurrentRegion):String = {
      val accountNumberTry = Try[String](AmazonIdentityManagementClientBuilder.defaultClient.listRoles().getRoles.get(0).getArn.split(":")(4))

      accountNumberTry match {
        case Success(account) => s"s3://aws-athena-query-results-$account-${region.getName}/"
        case Failure(e) => throw e
      }
    }

  }

}
