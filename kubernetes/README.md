# Usage

### Requirements

* [Docker](https://docs.docker.com/install/)
* [kubectl (Kubernetes command line)](https://kubernetes.io/docs/tasks/tools/install-kubectl/)
* A running Kubernetes cluster [(set up Minikube)](https://kubernetes.io/docs/setup/minikube/)

### Deploy

> Remember that before to deploy the Prediction REST service it's needed to create the Docker image first.

Run the following commands to deploy the Prediction REST service on top of **Kubernetes**:

```sh
kubectl create -f resources/namespace.yml
kubectl create -f resources/persistentVolumeClaim.yml
kubectl create -f resources/deployment.yml
kubectl create -f resources/service.yml
```

## Testing the services

One of the easiest ways to test the Prediction service, on a development environment, is using Kube-Proxy. Type the following commands to go for this approach:

```sh
kubectl proxy

curl -v -X POST \
  http://localhost:8001/api/v1/namespaces/force/services/side-prediction:http/proxy/prediction \
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
Note: Unnecessary use of -X or --request, POST is already inferred.
*   Trying ::1...
* TCP_NODELAY set
* Connection failed
* connect to ::1 port 8001 failed: Connection refused
*   Trying 127.0.0.1...
* TCP_NODELAY set
* Connected to localhost (127.0.0.1) port 8001 (#0)
> POST /api/v1/namespaces/force/services/side-prediction:http/proxy/prediction HTTP/1.1
> Host: localhost:8001
> User-Agent: curl/7.54.0
> Accept: */*
> Content-Type: application/json
> Content-Length: 107
> 
* upload completely sent off: 107 out of 107 bytes
< HTTP/1.1 503 Service Unavailable
< Content-Length: 0
< Content-Type: text/plain; charset=utf-8
< Date: Thu, 28 Mar 2019 20:28:03 GMT
< 
* Connection #0 to host localhost left intact
```

That's completely normal, as we didn't publish a model yet. The REST service have two possible outcomes:

* Code 200 and a JSON body, if it success.
* Code 503 and an empty body, if it fails.

After we publish our first model we should get the same response than we got in the service testing:

```sh
{"side":"Jedi","error":"less than 17%"}
```