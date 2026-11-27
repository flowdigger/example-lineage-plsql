# PL/SQL lineage: from source code to system knowledge

[![Build and Test](https://github.com/flowdigger/example-lineage-plsql/actions/workflows/ci.yml/badge.svg)](https://github.com/flowdigger/example-lineage-plsql/actions/workflows/ci.yml)
[![Coverage CI](https://github.com/flowdigger/example-lineage-plsql/actions/workflows/coverage.yml/badge.svg)](https://github.com/flowdigger/example-lineage-plsql/actions/workflows/coverage.yml)
[![CodeQL](https://github.com/flowdigger/example-lineage-plsql/actions/workflows/codeql.yml/badge.svg)](https://github.com/flowdigger/example-lineage-plsql/actions/workflows/codeql.yml)
[![Dependency Check](https://github.com/flowdigger/example-lineage-plsql/actions/workflows/dependency-check.yml/badge.svg)](https://github.com/flowdigger/example-lineage-plsql/actions/workflows/dependency-check.yml)
[![SpotBugs Security](https://github.com/flowdigger/example-lineage-plsql/actions/workflows/spotbugs-security.yml/badge.svg)](https://github.com/flowdigger/example-lineage-plsql/actions/workflows/spotbugs-security.yml)
[![Coverage](https://codecov.io/gh/flowdigger/example-lineage-plsql/graph/badge.svg)](https://codecov.io/gh/flowdigger/example-lineage-plsql)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

How can you discover where data comes from when the answer lives in code rather
than a diagram? This Java and Gradle project explores that question with an
Oracle PL/SQL example. It derives table lineage from Liquibase and PL/SQL
artifacts, checks expected relationships, and publishes artifacts you can inspect.
An ArchiMate model supplies architecture evidence; a separate Neo4j flow
demonstrates graph verification with stub data.

The broader idea is to treat technical artifacts as **evidence** and
reconstruct knowledge through explicit, reproducible transformations rather
than maintain lineage by hand. See
[From Data Lineage to System Knowledge](https://www.linkedin.com/pulse/from-data-lineage-system-knowledge-jurgen-hildebrand-kkpce/)
for the motivation and [Flowdigger's derivation lifecycle](https://flowdigger.github.io/core-concepts/derivation-lifecycle/)
for the pipeline.

## What this example does

The customer procedure reads `raw_customer` and `customer_reference`, writes
`valid_customer`, then reads `valid_customer` and writes `merged_customer`.
The lineage pipeline extracts DDL from the Liquibase changelogs, parses the
PL/SQL, derives and validates flows, and publishes lineage. The example-specific
verification checks these table relationships:

```text
dsa.raw_customer       -> dsa.valid_customer
dsa.customer_reference -> dsa.valid_customer
dsa.valid_customer     -> dsa.merged_customer
```

The pipeline uses **XIR** (eXtensible Intermediate Representation) for
structured intermediate artifacts. Think of it as a representation for
structured data, not the lineage model itself. You do not need deep XML or XIR
knowledge to run the example; learn the notation and transformation details
when you inspect or extend the pipeline.

### Current boundaries

- The lineage tasks extract source DDL from Liquibase files; they do not need to
  deploy an Oracle schema or execute the procedure to derive lineage. Separate
  Oracle integration tests exercise database behavior.
- `endToEndTest` orchestrates architecture export, lineage generation and
  graph verification, but the Neo4j flow loads **stub data**, not the published
  lineage artifacts. It does not prove that generated lineage reaches Neo4j.
- `verifyExampleLineage` checks the customer procedure and GraphML edges
  separately; `endToEndTest` does not invoke that specific verification task.
- Evidence traceability is a design goal, not a complete end-to-end guarantee.
  Provenance is carried through merge but remains incomplete after stitch;
  see [ADR-0011](https://flowdigger.github.io/adr/0011-preserve-provenance/)
  and [implementation status](https://flowdigger.github.io/lineage/#implementation-status).

## Get started

### Prerequisites

- JDK 21. Use the included Gradle wrapper; a separate Gradle installation is
  not required.
- Git and network access for dependencies, architecture tooling, and source
  synchronization.
- Archi runtime for the architecture export. The architecture Gradle plugin
  configures the Archi version used by this project.
- Docker for the Neo4j flow and Oracle integration tests.

The lineage pipeline syncs the configured `main` branch of
`flowdigger/example-lineage-plsql` into `build/lineage-source/` and consumes
DDL extracted from the **local** Liquibase files under
`build/projects/application/ddl/`. Be aware of this distinction when working
with local changes. If a sibling `gradle-lineage-plugin` checkout exists, the
build uses it; otherwise it resolves the configured plugin. CI checks out pinned
build logic.

### Generate and verify customer lineage

```bash
./gradlew generateLineage verifyExampleLineage
```

Start with the procedure in
[`004-procedure.xml`](2-application/src/main/resources/db/changelog/changes/004-procedure.xml),
then follow the generated artifacts:

| What to inspect | Default location |
|---|---|
| Extracted DDL | `build/projects/application/ddl/` |
| Parsed PL/SQL syntax as XIR | `build/lineage-uniform/1.parse/sql/application/ast/` |
| Semantic observations as XIR | `build/lineage-uniform/2.observe/sql/application/` |
| Published GraphML | `build/lineage-uniform/9.publish/artifacts/graphml/lineage.graphml` |
| Lineage report | `build/reports/lineage-report.html` |

`verifyExampleLineage` checks the procedure's AST and observation and the
three expected GraphML edges above. Published artifacts also include RDF, JSON,
and per-processor XIR. Set `-PbuildRoot=<path>` or `LINEAGE_BUILD_ROOT` to
change the default lineage output directory.

### Run the graph lifecycle

```bash
./gradlew endToEndTest
```

Inspect `build/reports/end-to-end-report.html` and the graph reports under
`build/projects/knowledgeGraph/graph-report/`. This runs the stub-backed graph
flow described above; use `verifyExampleLineage` as well when checking the
generated customer lineage.

## Explore and contribute

| Directory | Role |
|---|---|
| [`1-architecture`](1-architecture/) | ArchiMate model and architecture exports |
| [`2-application`](2-application/) | Spring Boot application, Liquibase changelogs and PL/SQL |
| [`3-lineage`](3-lineage/) | Pipeline configuration, reporting and example verification |
| [`4-knowledge-graph`](4-knowledge-graph/) | Neo4j stub flow, graph verification and metadata API emulator |

The root-applied lineage plugin is configured in
[`3-lineage/lineage-pipeline.gradle`](3-lineage/lineage-pipeline.gradle).
Start a contribution by tracing an existing source artifact through its
generated observation and published result, then add or update checks for the
behavior you change. See [ARCHITECTURE.md](ARCHITECTURE.md) for the module
overview and the [knowledge graph README](4-knowledge-graph/README.md) for
its API and tests.

Useful focused commands:

```bash
# Structure and cross-module contracts
./gradlew :application:test --tests name.jurgenei.example.lineage.GrowthStructureBootstrapTest --tests name.jurgenei.example.lineage.GrowthCrossModuleContractTest --tests name.jurgenei.example.lineage.GrowthEndToEndGraphVerificationContractTest

# Oracle integration test and knowledge graph module tests
./gradlew :application:test --tests name.jurgenei.example.lineage.LineageLoaderIntegrationTest
./gradlew :knowledgeGraph:test
```

Application and knowledge graph tests use Mockito's subclass mock maker. It
cannot mock final classes or methods, or static methods; tests requiring inline
mocking must explicitly enable Mockito's inline mock maker.

Gradle outputs live under `build/`; subproject outputs are under
`build/projects/<project-name>/`. `./gradlew clean` removes both.

## Learn more

- [Developer path](https://flowdigger.github.io/role-paths/developer/) and
  [evidence model](https://flowdigger.github.io/core-concepts/evidence-model/):
  concepts and extension points.
- [Nine pipeline phases](https://flowdigger.github.io/adr/0007-explicit-pipeline-phases/):
  parse, observe, derive, validate, merge, stitch and publish.
- [XIR quick reference](https://github.com/jurgenei/jurgenei/blob/main/xir-starter-kit/spec/XIR-QUICK-REFERENCE.md)
  and [language definition](https://github.com/jurgenei/jurgenei/blob/main/xir-starter-kit/spec/XIR-LANGUAGE.md):
  notation and full specification.
- [System knowledge learning series](https://flowdigger.github.io/learn/system-knowledge/):
  evidence, reproducibility, merge, stitch, provenance and projections.
