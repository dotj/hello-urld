# hello-urld

Requirements:
- Java 8 
- SBT 
- Docker

Building and running:

```sh
# To run directly with sbt
sbt run

# To build and deploy with Docker
sbt assembly
docker build -t scala-app .
docker run -dp 9999:9999 scala-app
```

Server will be running on `http://localhost:9999/`