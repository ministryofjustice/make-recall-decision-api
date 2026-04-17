WIP

In this subdirectory is a set of tools for automatically generating and publishing data documentation to confluence.

```bash
├── README.md - this file
│
├── src - source for creating/augmenting data documentation
│   ├── dictionary - source for augmenting the data dictionary
│   │   ├── metadata.json - contains data about each field for the data dictionary to augment that retrieved from the database
│   │   └── notice.html - contains text to display on the data dictionary confluence page
│   │   
│   ├── erd - source for the erd
│   │   ├── notice.html - contains text to display on the erd confluence page
│   │   └── recall-decision.puml - the plantuml source for the erd diagram
│   │   
│   └── physical - source for augmenting schemaspy
│       └── notice.html - the text to display on the physical data confluence page
│       
│       
└── tools - the tools that do the actual generation and publishing
    ├── dictionary - tooling to create the data dictionary
    │   ├── create-data-dictionary - custom node application to create the data dictionary
    │   │   ├── create-data-dictionary.js - entrypoint for application
    │   │   ├── eu-west-2-bundle.pem - certificate for connecting to AWS RDS using ssl
    │   │   ├── node_modules - vendored dependencies
    │   ├── publish.sh - publish the data dictionary to confluence
    │   └── run.sh - crete the data dictionary
    │   
    ├── erd - tooling to create the erd
    │   ├── processing - diagrams created using plantuml
    │   ├── publish.sh - publish the erd to confluence
    │   └── run.sh = generate the erd diagrams
    │   
    └── physical - tooling to create the physical diagrams
        ├── publish.sh  - publish the physical diagrams to confluence
        ├── run.sh - generate the physical diagrams
        └── schema-spy-report - location where schema-spy report is downloaded to
```

Creates 3 types of docs
- Physical (ie db schema)
- Dictionary (near enough a logical model)
- erd diagram 

Tooling
===

Physical
- Schemaspy https://schemaspy.org/

Dictionary
- create-data-dictionary

ERD
- PlantUML https://plantuml.com/

Overview
===

Each set of doc generation has two phases
- Run. This generates the docs (run.sh)
- Publish. This publishes the docs to Confluence (publish.sh)

The Run Phase
===

Physical
====
- A new pod in the namespace is created
- This uses the schemaspy image
- It runs schemaspy against your database
- The output of schemaspy is copied back down from the pod
- The pod is deleted

Dictionary
====
- A new pod in the namespace is created
- This uses the node image
- It copies create-data-dictionary into the pod
- It runs creare-data-dictionary against your database
- The output of create-data-dictionary is copied back down from the pod
- The pod is deleted

erd
====
- The script runs plantuml locally

The Publish Phase
===

Physical 
====
- The publish script scans the schema-spy output for diagrams and uploads them to confluence, then inserts them into a page

Dictionary
====
- The publish script takes the generated html fragment and uploads it to confluence

ERD
====
- The publish script scans the schema-spy output for diagrams and uploads them to confluence, then inserts them into a page


Running the data documentation generators
===

The intention is that these will be run within github actions.

To run locally, you'll need to setup environment variables. You can use the .env.EXAMPLE and inject the environment variables before running the script

env $(grep -v '^#' ../.env | xargs) ./publish.sh

Settings
===

Most settings are carried out through changing environment variables

Physical
====

Dictionary
====

ERD
====
