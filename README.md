# Making a Recall Decision API (`make-recall-decision-api`)

[![repo standards badge](https://img.shields.io/badge/dynamic/json?color=blue&style=for-the-badge&logo=github&label=MoJ%20Compliant&query=%24.data%5B%3F%28%40.name%20%3D%3D%20%22make-recall-decision-api%22%29%5D.status&url=https%3A%2F%2Foperations-engineering-reports.cloud-platform.service.justice.gov.uk%2Fgithub_repositories)](https://operations-engineering-reports.cloud-platform.service.justice.gov.uk/github_repositories#make-recall-decision-api)
[![CircleCI](https://circleci.com/gh/ministryofjustice/make-recall-decision-api/tree/main.svg?style=svg)](https://circleci.com/gh/ministryofjustice/make-recall-decision-api)

This is the backend service to help case officers make recommendations on recall decisions.

## Usage

The primary software client for this service is the related UI [make-recall-decision-ui].

### Limitations

- the exposed API is WIP: it has been created to satisfy the specific UI client; modifications to it may be
  appropriate for more general consumption.

### Code Style & Formatting


### Running the Service Locally

In order to spin up the service, it's related user interface ([make-recall-decision-ui]) and all their dependencies locally, run the following script:

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

### Notes for M1 Mac Users

If you're using an M1/arm based Mac, you'll need to also have a checkout of [hmpps-auth](https://github.com/ministryofjustice/hmpps-auth) alongside your checkouts of `make-recall-decision-ui` and `make-recall-decision-api`, and pass all of the start scripts the `-a` parameter:

```
./scripts/start-local-services.sh -a
```

This will build the `hmpps-auth` container image locally on your machine before starting things up. This is needed as the currently released container for `hmpps-auth` does not run properly on M1 macs.

### Running Tests

Run the following script to run all he tests locally (or to see how to run them):

```
./build.sh
```

### Swagger UI

[make-recall-decision-api]: https://github.com/ministryofjustice/make-recall-decision-api
[make-recall-decision-ui]: https://github.com/ministryofjustice/make-recall-decision-ui



