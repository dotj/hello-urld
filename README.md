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

## API (WIP)

```sh
# Using cURL on bash.
# (If using Windows, remember to escape the parenthesis accordingly.)

# Create a shortlink
curl -X POST \
  'http:/localhost:9999/shortlink' \
  -H 'Content-Type:application/json' \
  -d '{
    "alias": "ggg",
    "fullUrl": "google.com"
  }'


curl -X POST \
  'http:/localhost:9999/shortlink' \
  -H 'Content-Type:application/json' \
  -d '{
    "alias": "aaa",
    "fullUrl": "amazon.com"
  }'

# Delete a shortlink
curl -X DELETE 'http:/localhost:9999/shortlink/ggg'


# Edit a shortlink
curl -X PUT \
  'http:/localhost:9999/shortlink/ggg' \
  -H 'Content-Type:application/json' \
  -d '{
    "updatedUrl": "https://lmgtfy.app/"
  }'
```