package com.b2wdigital.spark

import java.util.Properties

import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClientBuilder
import com.amazonaws.services.identitymanagement.model.Role

import scala.util.{Failure, Success, Try}
import com.simba.athena.amazonaws.regions.{Region, Regions}
import org.apache.spark.sql.{DataFrame, DataFrameReader}

import scala.collection.JavaConverters._

package object athena {

  implicit class AthenaDataFrameReader(reader: DataFrameReader) {

    def athena(query: String): DataFrame = {

      athena(query, getCurrentRegion)
    }

    def athena(query: String, region:Region): DataFrame = {
      athena(query, region, getS3StagingDir())
    }

    def athena(query: String, region:Region, s3StatingDir:String): DataFrame = {
      reader.format("io.github.tmheo.spark.athena")
        .option(JDBCOptions.JDBC_TABLE_NAME, s"($query)")
        .option("region", region.getName)
        .option("s3_staging_dir", s3StatingDir)
        .load
    }

    def athena(query: String, properties: Properties): DataFrame = {
      val options = properties.asScala
      options += (JDBCOptions.JDBC_TABLE_NAME -> query)
      reader.format("io.github.tmheo.spark.athena").options(options).load
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

      val accountNumber = accountNumberTry match {
        case Success(account) => account
        case Failure(_) => "562821017172"
      }

      s"s3://aws-athena-query-results-$accountNumber-$region/"
    }

  }

}
