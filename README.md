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

# Run unit-tests (Generates report and saves it locally)
./gradlew clean test --info

# Build the docker image of the application
./gradlew bootBuildImage

# Start the application on Docker
docker-compose up
```

## Usage
Once the server is up and running - following APIs can be invoked to manage the calendar for users. 

Create TimeSlot API
```
# Create time-slot request.
curl --verbose -X POST --header 'Content-Type: application/json' --data '{"beginTime": "2026-07-07T10:30:00.000Z", "durationInMinutes": 15}' "http://localhost:8080/tdoodle/users/1023/time-slots"

# Create time-slot response.
{"slotId":4,"userId":1023,"beginTime":"2026-07-07T10:30:00Z","durationInMinutes":15,"free":true}

Creates a time-slot for user: 1023 at 2026-07-07T10:30:00.000Z lasting 15 minutes. slot-id can be used in subsequent APIs.
```

Get TimeSlots API
```
# Get time-slot request.
curl --verbose "http://localhost:8080/tdoodle/users/1023/time-slots?timeFrameBegin=2026-07-07T09:40:00Z&timeFrameEnd=2026-07-07T11:20:00Z&slotType=BUSY"

# Create time-slot response.
[{"slotId":2,"userId":1023,"beginTime":"2026-07-07T10:30:00Z","durationInMinutes":15,"free":false}]

Returns the list of time-slots for the given user, after filtering based on the supplied optional query parameters.
```

Create Meeting API
```
# Create meeting request.
curl --verbose -X POST --header 'Content-Type: application/json' --data '{"title": "Onboarding meeting", "description": "Onboarding session for Akhil", "participants": ["Akhil", "Tejumoluwa"]}' "http://localhost:8080/tdoodle/users/1023/time-slots/1/meetings"

# Create time-slot response.
{"meetingId":1,"timeSlotId":1,"userId":1023,"title":"Onboarding meeting","description":"Onboarding session for Akhil","participants":["Akhil","Tejumoluwa"]}

Creates and returns a meeting object. 
```

Modify TimeSlot API
```
# Create time-slot request.
curl --verbose -X PUT --header 'Content-Type: application/json' --data '{"beginTime": "2026-07-07T11:15:00.000Z", "durationInMinutes": 35}' "http://localhost:8080/tdoodle/users/1023/time-slots/3"

# Create time-slot response.
{"slotId":3,"userId":1023,"beginTime":"2026-07-07T11:15:00Z","durationInMinutes":35,"free":true}

Updates time-slot id: 3 for user: 1023, given that business validations are met.
```

Delete TimeSlot API
```
# Delete time-slot request.
curl --verbose -X DELETE "http://localhost:8080/tdoodle/users/1023/time-slots/3"

# Delete time-slot response.
Http 204 (No Content)

Deletes time-slot id: 3 for user: 1023, given that business validations are met.
```