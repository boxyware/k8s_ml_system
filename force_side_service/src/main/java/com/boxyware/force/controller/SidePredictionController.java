package com.boxyware.force.controller;

import java.io.IOException;

import com.boxyware.force.model.Subject;
import com.boxyware.force.model.Prediction;
import com.boxyware.force.service.SidePredictionService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
public class SidePredictionController {

  @Autowired
  private SidePredictionService predictor;

  @RequestMapping("/version")
  public String greeting() {
    return "{ \"version\": \"2.0.0\" }";
  }

  @RequestMapping(path = "/prediction", method = POST)
  public ResponseEntity<Prediction> predict(@RequestBody Subject subject) {
    try {
      return new ResponseEntity<>(
        predictor.predict(subject),
        HttpStatus.OK);

    } catch (IOException e) {
      e.printStackTrace();

      return new ResponseEntity<>(
        null, HttpStatus.SERVICE_UNAVAILABLE);
      }
  }
}