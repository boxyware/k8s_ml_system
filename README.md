# TL;DR

This Git repository contains the code used for the Medium article [**A Reactive Machine Learning system on top K8s**](https://medium.com/@manuel.lara_82510/a-reactive-machine-learning-system-on-top-of-kubernetes-a9c812796afe).

In this repository you will find a service that is capable to predict if a given subject will become a Jedi or Sith or none of them.

The intention of this project is to show how it's possible to implement a reactive architecture that allow us to build a realistic and production scale Machine Learning system using Kubernetes.

The service has been implemented using two main components:
* A Machine Learning model generator.
* A REST service that exposes the prediction model.

Both of them are deployed on top of Kubernetes as Docker containers, so in order to manage propery all components this repository aslo includes the Kubernetes resources to expose the entire service.

# Usage

To build each component go to the its folder and follow the instructions described in each specific subproject README.md file.

The best way to deploy this analytical system is following these steps:

1. A good starting point could be to build the prediction service. To do this, go to **force_side_predictor** folder and follow the isntructions described in the README.md that you can find there.
2. With the Docker image created in the previous point, the next step is to deploy the REST service on Kubernetes. To do this, go to **kubernetes** folder and follow the isntructions described in the README.md that you can find there.
3. The REST service has been deployed with a shared volume where the predictive model is supposed to it will be. Now is time to run a the model learning component. To do this, go to **spark_force_side_model** folder and follow the isntructions described in the README.md that you can find there.

>In the README.md file that you could find inside of the **kubernetes** folder there are an example about how to test the whole service.