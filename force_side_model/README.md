**Scala** project that generates a Force Side Prediction Model using Apache Spark. This model will be capable to predict if a subject will become to Jedi or to Sith, or to none of them.

# Usage

### Requirements

* [Docker](https://docs.docker.com/install/)
* [kubectl (Kubernetes command line)](https://kubernetes.io/docs/tasks/tools/install-kubectl/)
* A running Kubernetes cluster [(set up Minikube)](https://kubernetes.io/docs/setup/minikube/)
* [Scala 2.11.x](https://www.scala-lang.org/download/)
* [sbt 0.13.x](https://www.scala-sbt.org/1.0/docs/Setup.html)
* [Spark 2.4.0 bundle](https://spark.apache.org/downloads.html)

### Build & deploy

> If you want to avoid installing ```Scala```, ```sbt``` and ```Spark``` you can use some convinience Docker images that I've created for this purpose. This documentation has been elaborated assuming that you went for this approach.

To download them, do as follows:

```sh
docker pull boxyware/sbt
docker pull boxyware/spark
```

This image may also be used in a standalone fashion by defining the following aliases:

```sh
alias sbt='docker run --rm -it -v $(pwd):/app -v /var/run/docker.sock:/var/run/docker.sock boxyware/sbt'
alias spark-submit='docker run --rm -it --entrypoint /opt/spark/bin/spark-submit boxyware/spark'
alias spark-shell='docker run --rm -it -v $(pwd):/app boxyware/spark --packages "ml.combust.mleap:mleap-spark_2.11:0.13.0"'
```

With the previous commands we've created two aliases that will allow us to work with **sbt** and **Spark** in the same way than if they were installed as usual. The third one isn't really needed to deploy this application, it's just in case of someone wants to play with the ```spark-shell```.

>I've shared the Docker daemon socket in the first alias because this **Scala** project will be packaged as a Docker image after compile everything, so the ```sbt``` command will try to build the mentioned image.
>
>We also can find something wierd in the second alias, this project won't persist the ML model as we usually do using the tipical **Spark** method ```model.write.overwrite.save```, instead of that we'll use the ```MLeap``` library to save the model to prevent the need to use the **Spark** libraries to load it. To find more information about this library follow this [link](http://mleap-docs.combust.ml/).

With the previous aliases in place, the next step will be to package this Model Generation Application. To do this perform the following command:

```sh
sbt docker
```

The previous command will take a while because this project has been configured to create an Uber Jar file. The reason to do this is because it's difficult to manage the **MLeap** dependency with the current integration between **Spark** and **Kubernetes**.

Once the previous command finishes, we should have the Docker image published in our local registry.

>The ```sbt``` command will create the Docker image in the local registry, if **Kubernetes** is using a different one, the image must be uploaded to there.

Then, the next step is to submit the application on the **Spark** server using the special configuration for **Kubernetes**. To do that execute the following commands:

```sh
kubectl proxy --accept-hosts='^.*$'

spark-submit \
    --master k8s://http://host.docker.internal:8001 \
    --deploy-mode cluster \
    --name side-model \
    --class com.boxyware.force.SidePredictor \
    --conf spark.executor.instances=1 \
    --conf spark.kubernetes.namespace=force \
    --conf spark.kubernetes.container.image=boxyware/force-side-model \
    --conf spark.kubernetes.driver.volumes.persistentVolumeClaim.model-volume.mount.path=/models \
    --conf spark.kubernetes.driver.volumes.persistentVolumeClaim.model-volume.mount.readOnly=false \
    --conf spark.kubernetes.driver.volumes.persistentVolumeClaim.model-volume.options.claimName=model-pvc \
    local:///opt/spark/work/force.jar
```

The first command exposes **Kubernetes** through a proxy open on 8001 port. With this we will be able to send traffic to **Kubernetes** in the same way it would be deployed on our machine. The second command will deploy the Docker image as a **Spark** application using the native **Kubernetes** deployment available from 2.3.0 version.

>One of the most interesting points of this new way to work with **Spark** is that we won't need to deploy a **Spark** cluster at any moment.

In the second command there is another point that it's worth to highlight. In the *master* url setting we've used ```host.docker.internal``` as a host. This is a special variable available in all Docker containers that allow to them to open a communication to its Docker Host, the actual machine that is running the mentioned container, for this case, our own *localhost*. We can't use the word *localhost* becuase that command run inside of a container actually and if we would've used *localhost* the command would try to reach a **Kubernetes** master that should be running inside of the container itself. In our case, thanks to the proxy command, **Kubernetes** is like it would be deployed on our local.

So after run the second command, a Pod will be deployed, inside of a specific *Namespace* and claiming a *Persistent Volume* that will be used to save the generated model. That volume is the same than the Prediction Service will claim.

The Pod will work as a **Spark** job, that will died after complete its tasks, in this case to train a predictive model and save it in a *Persistent Volume*.

## Checking this component

After submit a job to Kubernetes, you should see an output like this:

```sh

...

2019-03-24 10:39:15 INFO  LoggingPodStatusWatcherImpl:54 - State changed, new state: 
         pod name: side-predictor-1553420351697-driver
         namespace: force
         labels: spark-app-selector -> spark-4dbbab201daf4ae9aa2da5ce7a7e21d8, spark-role -> driver
         pod uid: aee4aeba-4e18-11e9-8d6e-025000000001
         creation time: 2019-03-24T09:39:13Z
         service account name: default
         volumes: spark-local-dir-1, model-volume, spark-conf-volume, default-token-s7r88
         node name: docker-for-desktop
         start time: 2019-03-24T09:39:13Z
         container images: boxyware/side-predictor:latest
         phase: Running
         status: [ContainerStatus(containerID=docker://40c596c3ff949a2db15789e2402d869fbc0e4bbe9695f17d3deddb4152d81ae3, image=boxyware/side-predictor:latest, imageID=docker://sha256:a624c8ecf5f4b9c8e44958bd0554fdcfd8050331e8ebbd12ee4a3282e3b9c3a9, lastState=ContainerState(running=null, terminated=null, waiting=null, additionalProperties={}), name=spark-kubernetes-driver, ready=true, restartCount=0, state=ContainerState(running=ContainerStateRunning(startedAt=Time(time=2019-03-24T09:39:15Z, additionalProperties={}), additionalProperties={}), terminated=null, waiting=null, additionalProperties={}), additionalProperties={})]
2019-03-24 10:39:36 INFO  LoggingPodStatusWatcherImpl:54 - State changed, new state: 
         pod name: side-predictor-1553420351697-driver
         namespace: force
         labels: spark-app-selector -> spark-4dbbab201daf4ae9aa2da5ce7a7e21d8, spark-role -> driver
         pod uid: aee4aeba-4e18-11e9-8d6e-025000000001
         creation time: 2019-03-24T09:39:13Z
         service account name: default
         volumes: spark-local-dir-1, model-volume, spark-conf-volume, default-token-s7r88
         node name: docker-for-desktop
         start time: 2019-03-24T09:39:13Z
         container images: boxyware/side-predictor:latest
         phase: Succeeded
         status: [ContainerStatus(containerID=docker://40c596c3ff949a2db15789e2402d869fbc0e4bbe9695f17d3deddb4152d81ae3, image=boxyware/side-predictor:latest, imageID=docker://sha256:a624c8ecf5f4b9c8e44958bd0554fdcfd8050331e8ebbd12ee4a3282e3b9c3a9, lastState=ContainerState(running=null, terminated=null, waiting=null, additionalProperties={}), name=spark-kubernetes-driver, ready=false, restartCount=0, state=ContainerState(running=null, terminated=ContainerStateTerminated(containerID=docker://40c596c3ff949a2db15789e2402d869fbc0e4bbe9695f17d3deddb4152d81ae3, exitCode=0, finishedAt=Time(time=2019-03-24T09:39:35Z, additionalProperties={}), message=null, reason=Completed, signal=null, startedAt=Time(time=2019-03-24T09:39:15Z, additionalProperties={}), additionalProperties={}), waiting=null, additionalProperties={}), additionalProperties={})]
2019-03-24 10:39:36 INFO  LoggingPodStatusWatcherImpl:54 - Container final statuses:
         Container name: spark-kubernetes-driver
         Container image: boxyware/side-predictor:latest
         Container state: Terminated
         Exit code: 0
2019-03-24 10:39:36 INFO  Client:54 - Application side-predictor finished.
2019-03-24 10:39:36 INFO  ShutdownHookManager:54 - Shutdown hook called
2019-03-24 10:39:36 INFO  ShutdownHookManager:54 - Deleting directory /private/var/folders/fj/w8st900j4fxdhy0_50kqsn8m0000gn/T/spark-61d3f539-4cf7-4749-9290-f51d06174269
```

The previous log shows the different job status and its termination code, 0 in this case that means everything was fine.

We also can check what is the status in **Kubernetes**. Asking for our Pods we should see an output like this:

```sh
kubectl get pod -n force -a
NAME                                  READY     STATUS      RESTARTS   AGE
side-predictor-1553537127630-driver   0/1       Completed   0          27s
````

The previous output shows that the Pod is no longer running because it finished with the model generation. 