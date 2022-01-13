# hello-urld

Requirements:
- Java 8 
- SBT 
- Docker

Building and running:

```sh
# Before running service, we'll want to have a local instance of postgreSQL running
docker pull postgres
docker run --name local-postgres -e POSTGRES_PASSWORD=password -d -p 5432:5432 postgres

# To run service directly with sbt
sbt run

# To build and deploy service with Docker
sbt assembly
docker build -t scala-app .
docker run -dp 9999:9999 scala-app
```

Server will be running on `http://localhost:9999/`

## API (WIP)

```sh
# Using cURL on bash.
# (If using Windows, remember to escape the parenthesis accordingly.)

# Create a shortlink
curl -X POST \
  'http:/localhost:9999/shortlink' \
  -H 'Content-Type:application/json' \
  -d '{
    "token": "ggg",
    "redirectToUrl": "https://google.com"
  }'

curl -X POST \
  'http:/localhost:9999/shortlink' \
  -H 'Content-Type:application/json' \
  -d '{
    "token": "wiki",
    "redirectToUrl": "https://www.wikipedia.org/"
  }'

# Delete a shortlink
curl -X DELETE 'http:/localhost:9999/shortlink/ggg'

# Edit a shortlink
curl -X PUT \
  'http:/localhost:9999/shortlink/ggg' \
  -H 'Content-Type:application/json' \
  -d '{
    "newUrl": "https://lmgtfy.app/"
  }'

# Redirected shortlink at: http://localhost:9999/s/wiki
```