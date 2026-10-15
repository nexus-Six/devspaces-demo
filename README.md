# rest-service — OpenShift Dev Spaces demo

Quarkus sample used to show how a **devfile link** in GitHub opens an OpenShift Dev Spaces
workspace with the matching JDK — no local tool installs required.

| Branch | App version        | JDK  | Devfile image                         |
|--------|--------------------|------|----------------------------------------|
| `main` | `2.0.0-SNAPSHOT`   | 21   | `ubi9/openjdk-21`                      |
| `v1`   | `1.4.0-SNAPSHOT`   | 21   | `ubi9/openjdk-21`                      |

## Open in Dev Spaces (demo links)

Replace `DEVSPACES_URL` with your cluster (for example `https://devspaces.apps.example.com`)
and `YOUR_ORG/rest-service` with your GitHub repo.

**Current / latest (main, JDK 21):**

```text
https://DEVSPACES_URL/#https://github.com/YOUR_ORG/rest-service/tree/main
```

**Older release (v1) — for the “fix a bug without a laptop setup” story:**

```text
https://DEVSPACES_URL/#https://github.com/YOUR_ORG/rest-service/tree/v1
```

You can also paste a raw `devfile.yaml` URL; each branch’s `devfile.yaml` sets
`checkoutFrom.revision` to that branch and uses the matching OpenJDK image.

Before first use, update the `origin` remote under `projects` in each branch’s `devfile.yaml`
to your real GitHub URL.

## Demo app page

```shell
./mvnw quarkus:dev
```

Open <http://localhost:8080/> — the page shows the **app version** and the **running Java version**.

### Fake Jira ticket (Dev Spaces hand-off)

Open <http://localhost:8080/jira> — a mock Jira issue with an
**Open in OpenShift Dev Spaces** button that points at the `v1` branch / `devfile.yaml`.

Configure the factory link in `src/main/resources/application.properties`:

```properties
demo.devspaces.base-url=https://devspaces.apps.YOUR_CLUSTER
demo.github.repo=https://github.com/YOUR_ORG/rest-service
demo.devspaces.branch=v1
```

Classic hello endpoint: <http://localhost:8080/hello>

## Packaging

```shell
./mvnw package
```

Native build:

```shell
./mvnw package -Dnative
```

## Related Guides

- REST ([guide](https://quarkus.io/guides/rest)): Build RESTful web services and APIs using Jakarta REST
