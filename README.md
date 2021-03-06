# hello-urld

Requirements:
- Java 8
- Docker or SBT 1.5.5

Built off the `play-scala-slick-example` sample.

## Running the app locally

```sh
# To run directly with sbt
sbt run

# To run tests
sbt test

# Run with Docker
docker build -t hello-urld .
docker run -dp 9000:9000 hello-urld
```

Server will be running on `http://localhost:9000/`.

You should see a (very modest) web form for adding short links, which I used
for debugging, but the full API and example cURL requests are shown below.

## API
- Endpoints can be found in the [`conf.routes`](https://github.com/dotj/hello-urld/blob/main/conf/routes) file.

### Examples

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

# Redirected shortlink at: http://localhost:9000/s/{token}
# e.g., http://localhost:9000/s/ggg will redirect to https://lmgtfy.app/

# Analytics can be found at http://localhost:9000/analytics/{token}

# Adding basic auth
`curl -vv --basic -u myuser:mypass http://....`
`Authorization: Basic bXl1c2VyOm15cGFzcw==`

# Create a shortlink with auth
# -u myuser:mypass -> bXl1c2VyOm15cGFzcw==

# wrong auth, should not delete
curl -X DELETE \
  -H 'Authorization: Basic AAAA' \
  'http:/localhost:9000/shortlink/ggg'

# should work
curl -X DELETE \
  -H 'Authorization: Basic bXl1c2VyOm15cGFzcw==' \
  'http:/localhost:9000/shortlink/ggg'

```

##  To do / other considerations

- [x] Dockerize
  - The [`play-scala-slick-example`](https://github.com/playframework/play-samples/tree/2.8.x/play-scala-slick-example)
    repo I used as a base has both a development mode and a production mode. Using `sbt run` runs the app in 
    development mode, which automatically boots a dev database, so you can run the app locally.
  - To dockerize the app, we need to:
    - Set up production mode (see [docs](https://www.playframework.com/documentation/2.8.x/ProductionConfiguration),
       this is much more involved.). 
    - Figure out how to build the fat .jar properly.
    - *OR, figure out a way to run development mode in Docker.*
- [x] Implement unit tests
- [ ] Cron job (or some other processing service?) to deprecate expired shortlinks
- [ ] Use a randomized ID for shortlinks
  - The shortlinks are currently enumerated. Ideally I'd use a UUID or a randomly generated bigint depending on how 
    much usage we think we'd get. Since the database is already created, we'd need to migrate the data to a new table.
- [ ] Extend analytics API 
  - Add more views and filtering and sorting options.
  - This will depend heavily on product needs as well (i.e., are they using Google Analytics UTM params? Or will they be querying our service db?)
- [ ] Register users for auth
  - new table - user table - username, password
  - api to create/delete users
  - Use registered users in auth
