# music-shop-http4s-kafka-app

A service that uses kafka to consume 'Music Products' from and persist them in a 
postgres database.

## How to run locally

### Prerequisites

Download and install Docker from: https://docs.docker.com/desktop/

Download kafka for Scala 2.13 from: https://www.apache.org/dyn/closer.cgi?path=/kafka/2.7.0/kafka_2.13-2.7.0.tgz

Clone this repo locally

Install Postgres with a DB GUI application like DBeaver: https://dbeaver.io/

Create a postgres database:

`CREATE DATABASE musicshop`
config is set here: https://github.com/JackieDev/music-shop-http4s-kafka-app/blob/main/src/main/resources/application.conf

### Run
Start up docker on your machine and open up a terminal inside of the cloned repo and type:
`docker-compose -f docker-compose.yml up`

Then in another terminal also inside this cloned repo:
`sbt run`




