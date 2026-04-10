WIP

In this subdirectory is a set of tools for automatically generating and publishing data documentation to confluence.

├── README.md - this file
├── src - source for creating / augmenting 
│   ├── dictionary
│   │   ├── metadata.json
│   │   └── notice.html
│   ├── domain
│   │   ├── diagrams
│   │   │   └── recall-decision.png
│   │   ├── notice.html
│   │   ├── recall-decision.png
│   │   └── recall-decision.puml
│   └── physical
│       └── notice.html
└── tools - the tools that do the actual generation and publishing
    ├── dictionary
    │   ├── create-data-dictionary
    │   │   ├── create-data-dictionary.js
    │   │   ├── eu-west-2-bundle.pem
    │   │   ├── node_modules
    │   ├── publish.sh
    │   └── run.sh
    ├── domain
    │   ├── processing
    │   ├── publish.sh
    │   └── run.sh
    └── physical
        ├── publish.sh
        ├── run.sh
        └── schema-spy-report

Creates 3 types of docs
- Physical (ie db schema)
- Dictionary
- Domain diagram

Each set of doc generation has two phases
- Run. This generates the docs (run.sh)
- Publish. This publishes the docs to Confluence (publish.sh)

Physical
- Schema spy in a pod

Dictionary
- create-data-dictionary in a pod

Domain
- plantuml locally

The intention is that these will be run within github actions.

To run locally, you'll need to setup environment variables. You can use the .env.EXAMPLE and inject the environment variables before running the script

env $(grep -v '^#' ../.env | xargs) ./publish.sh
