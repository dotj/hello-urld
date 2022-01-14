# hello-urld

Requirements:
- Java 8 
- SBT 
- Docker

Built off the `play-scala-slick-example` sample.

Building and running:

```sh
# To run directly with sbt
sbt run

# To build and deploy with Docker
sbt assembly
docker build -t scala-app .
docker run -dp 9000:9000 scala-app
```

Server will be running on `http://localhost:9000/`

## API (WIP)

```sh
# Using cURL on bash.
# (If using Windows, remember to escape the parenthesis accordingly.)

# List all shortlinks
curl 'http:/localhost:9000/shortlink'

# Create a shortlink
curl -X POST \
  'http:/localhost:9000/shortlink' \
  -H 'Content-Type:application/json' \
  -d '{
    "token": "ggg",
    "redirectToUrl": "https://google.com"
  }'

curl -X POST \
  'http:/localhost:9000/shortlink' \
  -H 'Content-Type:application/json' \
  -d '{
    "token": "wiki",
    "redirectToUrl": "https://www.wikipedia.org/"
  }'

# Delete a shortlink
curl -X DELETE 'http:/localhost:9000/shortlink/2'

# Edit a shortlink
curl -X PUT \
  'http:/localhost:9000/shortlink/by-token/ggg' \
  -H 'Content-Type:application/json' \
  -d '{
    "redirectToUrl": "https://lmgtfy.app/"
  }'

# Redirected shortlink at: http://localhost:9999/s/wiki
```