# B2W Spark Athena Driver

This library provides a JDBC [AWS Athena](https://aws.amazon.com/pt/athena/) reader for [Apache Spark](https://spark.apache.org/).
This library is based on [tmheo/spark-athena](https://github.com/tmheo/spark-athena), but with some essential differences:

* Is based on [Symba Athena JDBC Driver](https://docs.aws.amazon.com/athena/latest/ug/connect-with-jdbc.html), the most recent version oficially supported by athena.
* Is actively maintained, once it is currently used in production at B2W, the biggest brazilian e-commerce.
* Is query driven, instead of table driven
* Uses AWS default credential chain, so, is capable of authenticating by credentials files and EC2 Instances.
* This is build for [Spark 2.4.0](https://spark.apache.org/releases/spark-release-2-4-0.html) and [Scala 2.11](https://www.scala-lang.org/download/2.11.12.html) (there are plans for scala 2.12 versions once spark support is stable) 

## Why we developed this:

* Once quering on athena instead of directly in S3, we can use smaller instances for spark workers
* No need for configuration and deploy hadoop dependencies for S3 management
* Can be used locally,  EC2 Instances, or other cloud
* You can use the same query performed on athena, no need adjustements

## How to use

Once imported the implicits on `io.github.tmheo.spark.athena._` you can just use athena function as an extensions for spark dataframe readers:

```scala
import org.apache.spark.sql.SparkSession
import com.b2w.ml.spark.athena._

val spark = SparkSession
              .builder
              .appName("Athena JDBC test")
              .master("local[*]")
              .getOrCreate // Or any other SparkSession instantiation

val stagingDir = s"s3://bucket/athena-query-results" // Directory where athena query will be saved

val query =
  """
    |SELECT data from my table LIMIT 10
  """.stripMargin

val df1 = spark
          .read
          .athena(query, stagingDir)
```

If you are on an EC2 instance you can just use default s3 output directory for your account `s"s3://aws-athena-query-results-${account}-us-east-1/`:

```scala
val df1 = spark
          .read
          .athena(query)
```
 
Other signatures of `athena` functions are:

* `def athena(query: String): DataFrame`
* `def athena(query: String, region:Region): DataFrame`
* `def athena(query: String, s3StatingDir:String, region:Region = Region.getRegion(Regions.US_EAST_1)): DataFrame`
* `def athena(query: String, properties: Properties): DataFrame`

The parameters are:

* `query`: The query string to be performed on athena (as you may think)
* `region`: AWS `Region` instance, the default is `US_EAST_1`
* `s3StatingDir: s3 path where athena should output results. The default is `s3://aws-athena-query-results-${account}-us-east-1/` (works only on EC2 instances)
* `properties`: A Java Properties object with the following parameters:
    * `region`: AWS region name, like in (documentation)[https://docs.aws.amazon.com/general/latest/gr/rande.html]
    * `s3_staging_dir`: the same as  `s3StatingDir`parameter
    * `user`: The AWS Access Key Id
    * `password`: The AWS Secret Key
