# Making a Recall Decision API (`make-recall-decision-api`)

[![repo standards badge](https://img.shields.io/badge/dynamic/json?color=blue&style=for-the-badge&logo=github&label=MoJ%20Compliant&query=%24.data%5B%3F%28%40.name%20%3D%3D%20%22make-recall-decision-api%22%29%5D.status&url=https%3A%2F%2Foperations-engineering-reports.cloud-platform.service.justice.gov.uk%2Fgithub_repositories)](https://operations-engineering-reports.cloud-platform.service.justice.gov.uk/github_repositories#make-recall-decision-api)
[![CircleCI](https://circleci.com/gh/ministryofjustice/make-recall-decision-api/tree/main.svg?style=svg)](https://circleci.com/gh/ministryofjustice/make-recall-decision-api)

This is the backend service to help probation practitioners (PP) and Senior Probation Officers (SPOs) make recommendations on recall decisions.

## Usage

The primary software client for this service is the related UI [make-recall-decision-ui].

### Upstream APIs

The MRD application relies on data being retrieved from a number of upstream services, including Delius, Assess Risk and Needs (ARN) and Create and Vary a Licence (CVL). 
There are a number of services within the backend codebase that retrieve data from these upstream APIs, transform the data and then send back to the frontend.

### HMPPS domain event topic

The HMPPS domain event topic allows MRD to fire 'events' that are relevant to other services. For example, when a PP starts a recommendation, the case in Delius should be updated with this information. There is a MRD-and-Delius integration service that reads these messages and creates contacts in Delius.

## Running the service locally

In order to start up the service, its related user interface ([make-recall-decision-ui]) and all their dependencies locally, run the following script:

```
./scripts/start-local-services.sh
```

- [make-recall-decision-api] will be running on port `8080` (http://localhost:8080)
- [make-recall-decision-ui] will be running on port `3000` (http://localhost:3000)

To reload [make-recall-decision-api] (i.e. following a code change), run the following:

```
./scripts/reload-local-service-api.sh
```

To reload [make-recall-decision-ui] (i.e. following a code change), run the following:

```
./scripts/reload-local-service-ui.sh
```

And to stop everything, simply run the following:

```
./scripts/stop-local-services.sh
```

### First time users

The first time the service is started on your machine may result in a database error along the lines of 'mrd_user cannot be found'

To fix this there are a couple of things that can be tried:

Automatic:
1. ```docker compose -f docker-compose-postgres.yml up```

2. Start the database locally with ```./scripts/start-local-development.sh``` which should create the database automatically.

Manual:
If the above fails to work, then you will need to try and create the user and database manually

1. Download a database client such as `pgadmin4` (will assume pgadmin is being used for the purpose of the below steps)

2. Check if you can create an 'mrd_user' user within pgadmin. If an error is received saying user cannot be created then follow steps 3 and 4.

3. Open a terminal and enter `psql` 

    3a. If `psql` produces this error: `psql: error: connection to server on socket "/tmp/.s.PGSQL.5432" failed: FATAL:  database "<name>" does not exist` then try these steps:

    3b. `brew update`

    3c. `brew install postgres` or `brew upgrade postgresql` (if already installed)

    3d. `brew services start postgresql`

    3e. `createdb`

    3f. `psql` - this should now enter the database terminal 

4. `\du` - check that there is at least one role returned with 'Superuser'

5. `sudo -u {replace with the superuser username returned in step 4} psql {replace with the superuser username returned in step 4}`

6. You should now be in a position to go back to pgadmin and create the `mrd_user` user role and then the `make_recall_decision` database

### Notes for M1 Mac users

If you're using an M1/arm based Mac, you'll need to also have a checkout of [hmpps-auth](https://github.com/ministryofjustice/hmpps-auth) alongside your checkouts of `make-recall-decision-ui` and `make-recall-decision-api`, and pass all of the start scripts the `-a` parameter:

```
./scripts/start-local-services.sh -a
```

This will build the `hmpps-auth` container image locally on your machine before starting things up. This is needed as the currently released container for `hmpps-auth` does not run properly on M1 macs.

## Testing
The project uses a variety of testing levels including unit testing, integration testing and functional testing 

Integration tests use Mockito to mock external services

Functional tests hit the real external services e.g. Delius APIs

### Functional test

The functional test is a black box test of the MRD API. It runs against the dev environment rather than mock services.

* Add SYSTEM_CLIENT_ID, SYSTEM_CLIENT_SECRET, and USER_NAME (Dev Delius user) to your environment variables in both your bash profile, and your IntelliJ environment variables under `Edit Run Configurations`
* Ensure Docker is running
* To start the functional tests from the command line run the script `./scripts/run-functional-test.sh`
* To run the test in the IDE first start `./scripts/start-local-development.sh`, `docker-compose-postgres.yml` and then use the Gradle task `functional-test-light` or run the functional test directly from Intellij

### Running Tests

Run the following script to run all the integration and unit tests locally:

```
docker-compose -f docker-compose-postgres.yml up
./gradlew check
```

or to run all tests with the service starting in Docker:

```
./build.sh
```

## Other 

### Code Style & Formatting

Check style is used to ensure the codebase conforms to a coding standard.

### Swagger UI

[make-recall-decision-api]: https://github.com/ministryofjustice/make-recall-decision-api
[make-recall-decision-ui]: https://github.com/ministryofjustice/make-recall-decision-ui

### Trouble-Shooting

#### BuildProperties dependency not satisfied.

You may see an error during IntelliJ test runs using the Sprint Test Runner, that declares a dependency not satisfied:
No bean of type BuildProperties could be found.

The reason this is happening is because build-info.properties is not found in the test classpath.  This file seems to be
generated by gradle.  When spring sees the file, it makes a BuildProperties bean available.  Changing the compiler output
path of each module to conform to gradle solves the problem.
