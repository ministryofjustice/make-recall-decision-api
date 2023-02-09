# make-recall-decision-api Run Book

## Service or system overview

### Business overview

This provides an API interface for the make recall decision service within HMPPS.

### Technical overview

Internal API based on Springboot, using Kotlin as the main language. Deployed in kubernetes (the HMPPS Cloud Platform) using the configuration found in [helm_deploy](helm_deploy).

### Service Level Agreements (SLAs)

Office hours (Mon-Fri, 09:00-17:00), best efforts.

### Service owner

The `make-recall-decision` team develops and runs this service.

Contact the [#making-recall-decisions](https://mojdt.slack.com/archives/C01D6R49H34) and [#make-recall-decisions-dev](https://mojdt.slack.com/archives/C03B57W0ALT) channels on slack.

### Contributing applications, daemons, services, middleware

- Springboot application based on [hmpps-template-kotlin](https://github.com/ministryofjustice/hmpps-template-kotlin).
- PostgreSQL database for recommendation data - configured via [cloud-platform-environments](https://github.com/ministryofjustice/cloud-platform-environments).
- AWS S3 for document storage - again, configured via [cloud-platform-environments](https://github.com/ministryofjustice/cloud-platform-environments).
- [gotenberg](https://gotenberg.dev/) for PDF rendering - included via a helm sub-chart.
- [CircleCI](https://circleci.com/) for CI/CD.

## System characteristics

### Hours of operation

Available 24/7.

### Infrastructure design

The application runs on the [HMPPS Cloud Platform](https://user-guide.cloud-platform.service.justice.gov.uk/) within the `make-recall-decision-<tier>` namespaces (where `tier` is `dev`, `preprod` or `prod`).

The main application runs as a deployment named `make-recall-decision-api`, with the following support deployments (all deployed as part of the same helm/kubernetes configuration):

- `make-recall-decision-api-gotenberg` - for PDF rendering.

The API is made available externally from the cluster via an Ingress.

See the `values-<tier>.yaml` files in the [helm_deploy](helm_deploy) directory for the current configuration of each tier.

### Security and access control

In order to gain access to the `make-recall-decision-<tier>` namespaces in kubernetes you will need to be a member of the [ministryofjustice](https://github.com/orgs/ministryofjustice) github organisation and a member of the [making-recall-decision](https://github.com/orgs/ministryofjustice/teams/making-recall-decision) (github) team. Once joined, you should have access to the cluster within 24 hours.

You will need to follow the [Cloud Platform User Guide](https://user-guide.cloud-platform.service.justice.gov.uk/documentation/getting-started/kubectl-config.html#how-to-use-kubectl-to-connect-to-the-cluster) to setup your access from there - use instructions for connecting to the `live` cluster.

### Throttling and partial shutdown

If there is an issue with the service where it is causing load on downstream services and it needs to be shutdown quickly the following command will reduce the number of pod replicas to zero:

```
kubectl -n make-recall-decision-<tier> scale deployment make-recall-decision-api --replicas=0
```

We do not currently have a strategy in place to throttle requests.

### Environmental differences

Infrastructure wise, all three tiers are identical, but `prod` has the following differences:

- It will have more pod replicas of the main application deployment.
- As this is live data, you **must** be SC cleared if you need log into the cluster and interact with the application pods or data held within. You **do not** however need to be SC cleared to make changes to the application and deploy via the CI pipelines.

### Tools

- [port-forward-db.sh](scripts/port-forward-db.sh) - allows you to connect to the PostreSQL database from your local machine (as external access is blocked).

## System configuration

### Configuration management

- Infrastructure is configured via [cloud-platform-environments](https://github.com/ministryofjustice/cloud-platform-environments).
- Application configuration is via [helm_deploy](helm_deploy).

### Secrets management

Secrets are stored within the `make-recall-decision-<tier>` namespaces in kubernetes.

Secrets with information from [cloud-platform-environments](https://github.com/ministryofjustice/cloud-platform-environments) will be managed via the terraform code in there.

The contents of the `make-recall-decision-api` secret are handled in the following way:

- `API_CLIENT_ID` - managed externally via tooling in the `hmpps-auth` project.
- `API_CLIENT_SECRET` - managed externally via tooling in the `hmpps-auth` project.
- `APPINSIGHTS_INSTRUMENTATIONKEY` - managed externally via the [dps-project-bootstrap](https://github.com/ministryofjustice/dps-project-bootstrap) tooling.

## System backup and restore

This is handled by the HMPPS Cloud Platform Team, but details of how each component is considered is below.

### Database

The application uses a NoSQL postgres database

## Monitoring and alerting

### Log aggregation solution

Please see [Confluence](https://dsdmoj.atlassian.net/wiki/spaces/MRD/pages/3987210241/Monitoring+Operability) for more details.

### Log message format

Currently the ELK solution cannot correctly process/transform structured/JSON logging, so a `log4j` single-line output is currently preferred.

### Events and error messages

Please see [Confluence](https://dsdmoj.atlassian.net/wiki/spaces/MRD/pages/3987210241/Monitoring+Operability#Runtime-Error-Reporting) for more details.

### Metrics

Please see [Confluence](https://dsdmoj.atlassian.net/wiki/spaces/MRD/pages/3987210241/Monitoring+Operability#Metrics-%26-Dashboards) for more details.

### Health checks

#### Health of dependencies

`/health` (i.e. https://make-recall-decision-api.hmpps.service.justice.gov.uk/health) checks and reports the health of all services and components that the application depends upon. A HTTP 200 response code indicates that everything is healthy.

You can see the services that this application depends on within the [application.yml file](src/main/resources/application.yml#L112-L149).

#### Health of service

`/health/liveness` (i.e. https://make-recall-decision-api.hmpps.service.justice.gov.uk/health/liveness) indicates that the application is started up, but does not indicate that it is ready to process work. A HTTP 200 response code indicates that the application has started.

`/health/readiness` (i.e. https://make-recall-decision-api.hmpps.service.justice.gov.uk/health/readiness) indicates that the application is ready to handle requests as it has checked its connections to all dependencies are working. A HTTP 200 response code indicates that the application has started and is ready to handle requests.

## Operational tasks

### Deployment

We use CircleCI to manage deployments (see [.circleci/config.yml](.circleci/config.yml) for the full configuration):

- Built docker images are pushed to [quay.io](https://quay.io/repository/hmpps/make-recall-decision-api).
- Deployment to kubernetes uses helm.

### Troubleshooting

Please see [Confluence](<https://dsdmoj.atlassian.net/wiki/spaces/MRD/pages/3987210241/Monitoring+Operability#Debugging-an-Application-That-Fails-to-Start>) for some generic troubleshooting notes.

## Maintenance tasks

### Identified vulnerabilities

We scan the currently deployed docker containers daily with [trivy](https://github.com/aquasecurity/trivy). If any `HIGH` or `CRITICAL` vulnerabilities are identified the team is notified in the [#make-recall-decisions-dev](https://mojdt.slack.com/archives/C03B57W0ALT) slack channel. **These issues should be fixed as soon as possible.**
