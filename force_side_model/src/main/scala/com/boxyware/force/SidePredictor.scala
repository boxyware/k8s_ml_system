package com.boxyware.force

import scala.io.Source

import java.nio.file.{Paths, Files}

import org.apache.spark.ml.tuning.CrossValidator
import org.apache.spark.ml.{Pipeline, PipelineModel}
import org.apache.spark.sql.{SparkSession, DataFrame}
import org.apache.spark.ml.classification.{NaiveBayes, NaiveBayesModel}
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator
import org.apache.spark.ml.feature.{QuantileDiscretizer, VectorAssembler, StringIndexer, IndexToString}

import ml.combust.mleap.spark.SimpleSparkSerializer;

object SidePredictor {

  // Note that applications should define a main() method instead of extending scala.App.
  // Subclasses of scala.App may not work correctly.
  def main(args: Array[String]) {
    val spark = SparkSession.builder().master("local").appName("Force Side").getOrCreate()
    import spark.implicits._

    val data = TestDataset.subjects.toDF

    // Create the training and testing data
    val Array(trainingData, testingData) = data.randomSplit(Array(0.8, 0.2), seed = 1L)

    // Extract features
    val midichlorianDiscretizer = new QuantileDiscretizer()
      .setInputCol("midichlorian")
      .setOutputCol("midichlorianId")
      .setNumBuckets(4)

    val speciesIndexer = new StringIndexer()
      .setInputCol("species")
      .setOutputCol("speciesId")
      .setHandleInvalid("keep")         // In the sample doesn't appear all the possible values

    val genderIndexer = new StringIndexer()
      .setInputCol("gender")
      .setOutputCol("genderId")

    val homeworldIndexer = new StringIndexer()
      .setInputCol("homeworld")
      .setOutputCol("homeworldId")
      .setHandleInvalid("keep")         // In the sample doesn't appear all the possible values

    val assembler = new VectorAssembler()
      .setInputCols(Array("midichlorianId", "speciesId", "genderId", "homeworldId"))
      .setOutputCol("features")

    val sideIndexer = new StringIndexer()
      .setInputCol("side")
      .setOutputCol("label")

    val classifier = new NaiveBayes()

    val labelConverter = new IndexToString()
      .setInputCol("prediction")
      .setOutputCol("predictedLabel")
      .setLabels(sideIndexer.fit(trainingData).labels)

    val pipeline = new Pipeline()
      .setStages(Array(midichlorianDiscretizer, speciesIndexer,
        genderIndexer, homeworldIndexer, assembler, sideIndexer, classifier, labelConverter))

    // Train the model
    val model = pipeline.fit(trainingData)

    // Test the model
    val accuracy = evaluateModel(model, testingData)

    println(s"Test set accuracy = ${accuracy}")

    // Publish the model (if proceede)
    if (accuracy > 0.6) {
      val path = "/models/side-naivebayes.zip"
      serialiseMleapBundle(model, trainingData, path)

      println(s"New model has been published on ${path}.")
    }
  }

  def evaluateModel(model: PipelineModel, data: DataFrame): Double = {
    val predictions = model.transform(data)
    
    predictions.select("prediction", "label", "predictedLabel").show

    val evaluator = new MulticlassClassificationEvaluator()
      .setLabelCol("label")
      .setPredictionCol("prediction")
      .setMetricName("accuracy")

    evaluator.evaluate(predictions)
  }

  def serialiseMleapBundle(model: PipelineModel,
      data: DataFrame, path: String) {

    val dftWithPredictions = model.transform(data)
    val serialiser = new SimpleSparkSerializer()

    Files.deleteIfExists(Paths.get(path))

    serialiser.serializeToBundle(
      model,
      s"jar:file:${path}",
      dftWithPredictions);
  }
}