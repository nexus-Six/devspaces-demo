#!/usr/bin/env bash
# Pre-pull Dev Spaces / demo images onto every OpenShift node.
#
# Usage:
#   ./openshift/prepull/prepull.sh              # apply DaemonSet and wait until Ready
#   ./openshift/prepull/prepull.sh --status     # show DaemonSet status
#   ./openshift/prepull/prepull.sh --remove     # delete the DaemonSet (images stay cached)
#   ./openshift/prepull/prepull.sh --che-puller # also enable CheCluster Kubernetes Image Puller
#
set -euo pipefail

ROOT="$(cd "$(dirname "$0")" && pwd)"
IMAGES_FILE="${ROOT}/images.txt"
NAMESPACE="${PREPULL_NAMESPACE:-openshift-operators}"
DS_NAME=devspaces-image-prepull
MODE=apply

for arg in "$@"; do
  case "$arg" in
    --remove) MODE=remove ;;
    --status) MODE=status ;;
    --che-puller) MODE=che-puller ;;
    --help|-h)
      sed -n '2,12p' "$0"
      exit 0
      ;;
    *)
      echo "Unknown argument: $arg" >&2
      exit 1
      ;;
  esac
done

need_oc() {
  command -v oc >/dev/null || { echo "oc is required" >&2; exit 1; }
  oc whoami >/dev/null || { echo "Not logged in to OpenShift (oc whoami failed)" >&2; exit 1; }
}

slug() {
  echo "$1" \
    | sed -E 's|^[^/]+/||; s|[@:]|-|g; s|/|-|g; s|[^a-z0-9-]|-|g; s|-+|-|g; s|^-||; s|-$||' \
    | cut -c1-50 \
    | sed 's|-$||'
}

read_images() {
  grep -vE '^\s*(#|$)' "${IMAGES_FILE}"
}

ensure_pull_secret() {
  # Copy the cluster global pull secret so registry.redhat.io pulls succeed.
  if ! oc -n "${NAMESPACE}" get secret prepull-redhat-pull-secret >/dev/null 2>&1; then
    echo "Installing cluster pull secret into ${NAMESPACE}/prepull-redhat-pull-secret"
    oc get secret pull-secret -n openshift-config -o json \
      | python3 -c '
import json,sys
s=json.load(sys.stdin)
s["metadata"]={"name":"prepull-redhat-pull-secret","namespace":"'"${NAMESPACE}"'"}
print(json.dumps(s))
' | oc apply -f -
  fi
}

build_daemonset() {
  local img name idx=0
  echo "apiVersion: apps/v1"
  echo "kind: DaemonSet"
  echo "metadata:"
  echo "  name: ${DS_NAME}"
  echo "  namespace: ${NAMESPACE}"
  echo "  labels:"
  echo "    app.kubernetes.io/name: ${DS_NAME}"
  echo "    app.kubernetes.io/part-of: devspaces-demo"
  echo "spec:"
  echo "  selector:"
  echo "    matchLabels:"
  echo "      app.kubernetes.io/name: ${DS_NAME}"
  echo "  updateStrategy:"
  echo "    type: RollingUpdate"
  echo "  template:"
  echo "    metadata:"
  echo "      labels:"
  echo "        app.kubernetes.io/name: ${DS_NAME}"
  echo "        app.kubernetes.io/part-of: devspaces-demo"
  echo "    spec:"
  echo "      tolerations:"
  echo "        - operator: Exists"
  echo "      imagePullSecrets:"
  echo "        - name: prepull-redhat-pull-secret"
  echo "      initContainers:"
  while IFS= read -r img; do
    name="pull-$(slug "$img")-${idx}"
    idx=$((idx + 1))
    echo "        - name: ${name}"
    echo "          image: ${img}"
    echo "          imagePullPolicy: IfNotPresent"
    # RHEL-based Dev Spaces images include /bin/true; this forces a pull then exits.
    echo "          command: [\"/bin/true\"]"
    echo "          resources:"
    echo "            requests:"
    echo "              cpu: 10m"
    echo "              memory: 32Mi"
    echo "            limits:"
    echo "              cpu: 200m"
    echo "              memory: 256Mi"
  done < <(read_images)
  if [[ "${idx}" -eq 0 ]]; then
    echo "No images listed in ${IMAGES_FILE}" >&2
    exit 1
  fi
  echo "      containers:"
  echo "        - name: pause"
  echo "          image: registry.k8s.io/pause:3.9"
  echo "          imagePullPolicy: IfNotPresent"
  echo "          resources:"
  echo "            requests:"
  echo "              cpu: 1m"
  echo "              memory: 8Mi"
  echo "            limits:"
  echo "              cpu: 10m"
  echo "              memory: 16Mi"
  echo "      terminationGracePeriodSeconds: 5"
}

wait_ready() {
  echo "Waiting for DaemonSet ${DS_NAME} to be ready in ${NAMESPACE}..."
  local desired ready i
  for i in $(seq 1 180); do
    desired="$(oc -n "${NAMESPACE}" get ds "${DS_NAME}" -o jsonpath='{.status.desiredNumberScheduled}' 2>/dev/null || echo 0)"
    ready="$(oc -n "${NAMESPACE}" get ds "${DS_NAME}" -o jsonpath='{.status.numberReady}' 2>/dev/null || echo 0)"
    echo "  [${i}] ready=${ready}/${desired}"
    if [[ "${desired}" != "0" && "${ready}" == "${desired}" ]]; then
      echo "All nodes have prepulled images."
      return 0
    fi
    # Surface pull errors early
    if (( i % 6 == 0 )); then
      oc -n "${NAMESPACE}" get pods -l app.kubernetes.io/name="${DS_NAME}" \
        -o custom-columns=NAME:.metadata.name,STATUS:.status.phase,RESTARTS:.status.containerStatuses[0].restartCount 2>/dev/null || true
    fi
    sleep 10
  done
  echo "Timed out waiting for prepull DaemonSet." >&2
  oc -n "${NAMESPACE}" describe ds "${DS_NAME}" | tail -40 || true
  oc -n "${NAMESPACE}" get pods -l app.kubernetes.io/name="${DS_NAME}" -o wide || true
  return 1
}

enable_che_puller() {
  local che_ns che_name images_csv img short
  che_ns="$(oc get checluster -A -o jsonpath='{.items[0].metadata.namespace}' 2>/dev/null || true)"
  che_name="$(oc get checluster -A -o jsonpath='{.items[0].metadata.name}' 2>/dev/null || true)"
  if [[ -z "${che_ns}" || -z "${che_name}" ]]; then
    echo "No CheCluster found; skip --che-puller" >&2
    return 1
  fi

  images_csv=""
  while IFS= read -r img; do
    short="$(slug "$img" | cut -c1-40)"
    if [[ -n "${images_csv}" ]]; then
      images_csv+=";"
    fi
    images_csv+="${short}=${img}"
  done < <(read_images)

  echo "Enabling Kubernetes Image Puller on CheCluster ${che_ns}/${che_name}"
  oc patch checluster "${che_name}" -n "${che_ns}" --type merge -p "{
    \"spec\": {
      \"devEnvironments\": {
        \"imagePuller\": {
          \"enable\": true,
          \"spec\": {
            \"images\": \"${images_csv}\"
          }
        }
      }
    }
  }"
  echo "CheCluster imagePuller enabled."
}

need_oc

case "${MODE}" in
  remove)
    oc -n "${NAMESPACE}" delete ds "${DS_NAME}" --ignore-not-found
    echo "Removed DaemonSet ${DS_NAME} (node image cache retained)."
    ;;
  status)
    oc -n "${NAMESPACE}" get ds "${DS_NAME}" -o wide 2>/dev/null || echo "DaemonSet not found"
    oc -n "${NAMESPACE}" get pods -l app.kubernetes.io/name="${DS_NAME}" -o wide 2>/dev/null || true
    ;;
  che-puller)
    enable_che_puller
    ;;
  apply)
    oc get ns "${NAMESPACE}" >/dev/null
    ensure_pull_secret
    echo "Applying prepull DaemonSet to namespace ${NAMESPACE} with images:"
    read_images | sed 's/^/  - /'
    build_daemonset | oc apply -f -
    wait_ready
    echo
    echo "Tip: leave the DaemonSet running to refresh caches after node reprovisions,"
    echo "  or remove it with: $0 --remove"
    echo "Optional: enable the built-in Che image puller with: $0 --che-puller"
    ;;
esac
