**Java** project that publishes as a REST service a prediction model capable to predict if a person will become to Jedi or to Sith, or to none.

This Java project use *Apache Maven* to manage the dependencies of the following frameworks:
* [MLeap](http://mleap-docs.combust.ml/)
* [Spring Boot](https://spring.io/projects/spring-boot)

>Follow the links for further information about these frameworks.

# Usage

### Requirements

* [Docker](https://docs.docker.com/install/)
* [Java 1.8.x](https://docs.oracle.com/javase/8/docs/technotes/guides/install/install_overview.html)
* [Maven](https://maven.apache.org/install.html)

### Build & deploy

> If you want avoid to isntall ```Java``` or ```Maven```, you can use the official Maven Docker image. This documentation has been elaborated assuming that you chose this approach.

To download the Maven Docker image, do as follows:

```sh
docker pull maven
```

This image may also be used in a standalone fashion by defining the following alias:

```sh
alias mvn='docker run --rm -it -v $(pwd):/usr/src/mymaven -w /usr/src/mymaven maven mvn'
```

With the previous commands we've created an alias that will allow us to work with ```Maven``` in the same way than we could do if it was installed as usual.

The next step will be to package this REST service application. To do this perform the following command:

```sh
mvn package
```

The previous command will package the REST application packaged as a JAR file, that could be found under ```target``` directory.

After that, the next step is to publish the application as a Docker image to be able to deploy this component on Kubernetes. We could also use Maven to automate the Docker image creation but for this case I chose to do it using a Dockerfile approach. So to create the Docker image for the REST service, execute the following commands:

```sh
cp src/main/resources/Dockerfile target/
docker build -t boxyware/force-side-service target/
```

>The previous commands will create the Docker image in the local registry, if Kubernetes is using a different one, the image must be uploaded to the proper registry.

## Checking this component

After create the Docker image wrapper, we can test if it works as expected. To do this, do as follows:

```sh
docker run -p 8080:8080 -v $(pwd)/src/main/resources/side-naivebayes.zip:/models/force/side-naivebayes.zip boxyware/force-side-service

curl -X POST \
  http://localhost:8080/prediction \
  -H 'Content-Type: application/json' \
  -d '{
	"name": "Manuel Lara",
	"midichlorian": 13000,
	"species": "human",
	"gender": "male",
	"homeworld":"Naboo"
}'
```

After run the ```curl``` command you should see a response like this:

```sh
{"side":"Jedi","error":"less than 17%"}
```