package com.boxyware.force.service;

import java.io.File;
import java.util.List;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import com.boxyware.force.model.Subject;
import com.boxyware.force.model.Prediction;

import org.springframework.stereotype.Service;

import ml.combust.bundle.dsl.Bundle;
import ml.combust.mleap.runtime.frame.Row;
import ml.combust.mleap.runtime.MleapContext;
import ml.combust.mleap.core.types.StructField;
import ml.combust.mleap.runtime.frame.Transformer;
import ml.combust.mleap.runtime.frame.DefaultLeapFrame;
import ml.combust.mleap.runtime.javadsl.LeapFrameBuilder;
import ml.combust.mleap.runtime.javadsl.BundleBuilderSupport;

import scala.util.Try;
import scala.collection.Seq;
import scala.collection.JavaConverters;

@Service
public class SidePredictionService {

  private static final Seq<String> PREDICTION_FIELD =
    JavaConverters.collectionAsScalaIterableConverter(
      Collections.singletonList("predictedLabel")).asScala().toSeq();

  public Prediction predict(Subject subject) throws IOException {
    File bundleFile = new File("/models/force/side-naivebayes.zip");
    Bundle<Transformer> transformerBundle = new BundleBuilderSupport()
      .load(bundleFile,
      MleapContext.defaultContext());

    DefaultLeapFrame leapFrame = getDataset(subject);

    return extractPrediction(transformerBundle.root().transform(leapFrame));
  }

  private Prediction extractPrediction(Try<DefaultLeapFrame> transformedLF) {
    if (transformedLF.isSuccess()) {
      
      Try<DefaultLeapFrame> prediction =
        transformedLF.get().select(PREDICTION_FIELD);

      if (prediction.isSuccess()) {
        return new Prediction(
          prediction.get().dataset().apply(0).getString(0)
            , "less than 17%");
      } else {
        System.out.println("Failed to calculate the prediction");
      }
    } else {
      System.out.println("Failed to execute model");
    }

    return new Prediction("NULL", "100%");            // This ensure the responsivness
  }

  private DefaultLeapFrame getDataset(Subject subject) {
    LeapFrameBuilder builder = new LeapFrameBuilder();

    List<StructField> fields = new ArrayList<StructField>();
    fields.add(builder.createField("name", builder.createString()));
    fields.add(builder.createField("midichlorian", builder.createDouble()));
    fields.add(builder.createField("species", builder.createString()));
    fields.add(builder.createField("gender", builder.createString()));
    fields.add(builder.createField("homeworld", builder.createString()));
    fields.add(builder.createField("side", builder.createString()));

    List<Row> rows = new ArrayList<Row>();
    rows.add(builder.createRow(
      subject.getName(),
      subject.getMidichlorian(),
      subject.getSpecies(),
      subject.getGender(),
      subject.getHomeworld(),
      "None"));
    
    return new DefaultLeapFrame(
      builder.createSchema(fields), rows);
  }
}
