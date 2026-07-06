<h3 style="text-align: center">
    A tiny calendar application !
</h3>

## Tech Stack

* Java 21
* Spring Boot
* Postgres
* Flyway
* Gradle
* Docker

## Requirements
* Java 21
* Gradle
* Docker

## Getting started

Open a terminal and cd into the project root.
```shell script
# Build Gradle Wrapper 
gradle wrapper

# Build the docker image of the application
./gradlew bootBuildImage

# Start the application on Docker
docker-compose up
```
