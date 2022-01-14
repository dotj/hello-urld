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

# TODO - Fix docker deployment issues
# To build and deploy with Docker
sbt assembly
docker build -t scala-app .
docker run -dp 9000:9000 scala-app
```

Server will be running on `http://localhost:9000/`

## API examples

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
    "redirectToUrl": "https://google.com",
    "expirationDate": "2022-02-02"
  }'

curl -X POST \
  'http:/localhost:9000/shortlink' \
  -H 'Content-Type:application/json' \
  -d '{
    "token": "wiki",
    "redirectToUrl": "https://www.wikipedia.org/"
  }'

# token and expirationDate are optional
curl -X POST \
  'http:/localhost:9000/shortlink' \
  -H 'Content-Type:application/json' \
  -d '{
    "redirectToUrl": "https://www.nytimes.com/"
  }'

# Delete a shortlink by token
curl -X DELETE 'http:/localhost:9000/shortlink/wiki'

# Edit a shortlink by token
curl -X PUT \
  'http:/localhost:9000/shortlink/ggg' \
  -H 'Content-Type:application/json' \
  -d '{
    "redirectToUrl": "https://lmgtfy.app/"
  }'

# Expiring endpoints
# Mocking an expired shortlink
curl -X POST \
  'http:/localhost:9000/shortlink' \
  -H 'Content-Type:application/json' \
  -d '{
    "token": "yyy",
    "redirectToUrl": "https://yahoo.com",
    "expirationDate": "2012-01-01"
  }'

# Manually deprecate links with an expiration date before today
curl -X PUT 'http:/localhost:9000/deprecate-shortlinks'

# Redirected shortlink at: http://localhost:9999/s/{token}
# e.g., http://localhost:9999/s/ggg will redirect to https://lmgtfy.app/
```