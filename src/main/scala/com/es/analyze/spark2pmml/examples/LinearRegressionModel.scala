import java.io.FileOutputStream
import javax.xml.transform.stream.StreamResult
import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.regression.LinearRegressionModel
import org.apache.spark.ml.regression.LinearRegression
import org.apache.spark.ml.feature.{RFormula}
import org.apache.spark.sql.types._
import org.apache.spark.sql.SparkSession
import org.jpmml.model.JAXBUtil
import org.jpmml.sparkml.{PMMLBuilder, ConverterUtil}

object LinearRegressionModel {
  def main(args: Array[String]) {
    val master = args(0)
    val mpgPath = args(1)
    val outPmmlFile = args(2)
    //val mpgPath="hdfs://master:8020/user/hdfs/data/dataset/auto-mpg.csv"
    //val outPmmlFile="/home/hdfs/pmml/spark2pmml_regression_Linear.pmml"
    val spark = SparkSession.builder.
      master(args(0)).
      appName("LinearRegressionModel").
      getOrCreate()

    val schema = new StructType(Array(StructField("mpg", DoubleType),
      StructField("cylinders", DoubleType), StructField("displacement", DoubleType),
      StructField("horsepower", DoubleType), StructField("weight", DoubleType), StructField("acceleration", DoubleType)
      , StructField("model_year", DoubleType), StructField("origin", DoubleType)))

    val mpgData = spark.read.option("header", "true").option("delimiter", " ").schema(schema).csv(mpgPath)

    val rFormula = new RFormula
    val formula: RFormula = rFormula.setFormula("mpg ~ cylinders + displacement + horsepower +" +
      " weight+acceleration+model_year+origin")

    val regression = new LinearRegression().setLabelCol(formula.getLabelCol).setFeaturesCol(formula.getFeaturesCol)

    val pipeline: Pipeline = new Pipeline setStages Array(formula, regression)
    val pipelineModel = pipeline.fit(mpgData)

    val pmml = new PMMLBuilder(mpgData.schema, pipelineModel).build()
    JAXBUtil.marshalPMML(pmml, new StreamResult(new FileOutputStream(outPmmlFile)))
  }
}