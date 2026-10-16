# rest-service — OpenShift Dev Spaces demo

Quarkus sample used to show how a **devfile link** in GitHub opens an OpenShift Dev Spaces
workspace with the **matching JDK** — no local tool installs required.

Different branches intentionally use **different JDKs** so the demo illustrates the
“wrong toolchain on my laptop” problem that Dev Spaces solves:

| Branch | App version        | JDK  | Devfile image                         |
|--------|--------------------|------|----------------------------------------|
| `main` | `2.0.0-SNAPSHOT`   | 21   | `ubi9/openjdk-21` (+ git)              |
| `v1`   | `1.4.0-SNAPSHOT`   | 17   | `ubi9/openjdk-17` (+ git)              |

## Open in Dev Spaces (demo links)

Replace `DEVSPACES_URL` with your cluster (for example `https://devspaces.apps.example.com`).

**Current / latest (`main`, JDK 21):**

```text
https://DEVSPACES_URL/#https://github.com/nexus-Six/devspaces-demo/tree/main
```

**Maintenance line (`v1`, JDK 17) — “fix a bug without installing an old JDK”:**

```text
https://DEVSPACES_URL/#https://github.com/nexus-Six/devspaces-demo/tree/v1
```

Each branch’s `devfile.yaml` sets `checkoutFrom.revision` to that branch and builds a tools
image from `.devfile/Dockerfile` with the matching OpenJDK + `git`.

## Demo app page

```shell
./mvnw quarkus:dev
```

Open <http://localhost:8080/> locally — the page shows the **app version** and the **running Java version**.

In **OpenShift Dev Spaces**, do not use `localhost` from your laptop browser. After the app starts, open the
published **http-app** endpoint from the IDE Ports / popup (“Open in New Tab”). The app binds to
`0.0.0.0:8080` via `quarkus.http.host` so that Route can reach it even when you launch with
**Run Quarkus** (not only the `quarkus-dev` task).

### Fake Jira ticket (Dev Spaces hand-off)

Open <http://localhost:8080/jira> — a mock Jira issue with an
**Open in OpenShift Dev Spaces** button that points at the `v1` branch (JDK 17) / `devfile.yaml`.

Configure the factory link: put the cluster Dev Spaces URL in a local **`.env`**
(gitignored — copy from `.env.example`), or rely on the workshop default in
`application.properties` / the `DEMO_DEVSPACES_BASE_URL` env in `devfile.yaml`.

```shell
cp .env.example .env
# edit DEMO_DEVSPACES_BASE_URL=https://devspaces.apps.YOUR_CLUSTER
```

Repo / branch defaults live in `src/main/resources/application.properties`:

```properties
demo.github.repo=https://github.com/nexus-Six/devspaces-demo
demo.devspaces.branch=v1
demo.devspaces.jdk=17
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
