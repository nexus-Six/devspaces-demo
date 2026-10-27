#!/usr/bin/env bash
# Apply Tekton CI for the Quarkus shop demo into the "shop" project.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")" && pwd)"
NS=shop
DOMAIN="$(oc get ingresses.config.openshift.io cluster -o jsonpath='{.spec.domain}')"

oc get project "${NS}" >/dev/null 2>&1 || oc new-project "${NS}"
oc project "${NS}"

# Ensure pipeline SA can build & deploy in this namespace
oc get sa pipeline -n "${NS}" >/dev/null 2>&1 || oc create sa pipeline -n "${NS}"
oc policy add-role-to-user edit system:serviceaccount:${NS}:pipeline -n "${NS}" >/dev/null
oc adm policy add-scc-to-user pipelines-scc -z pipeline -n "${NS}" 2>/dev/null \
  || oc adm policy add-scc-to-user anyuid -z pipeline -n "${NS}" 2>/dev/null \
  || true

# Webhook shared secret (create once; reuse on re-run)
if ! oc get secret github-webhook-secret -n "${NS}" >/dev/null 2>&1; then
  TOKEN="$(python3 -c 'import secrets; print(secrets.token_urlsafe(24))')"
  oc create secret generic github-webhook-secret \
    -n "${NS}" \
    --from-literal=secretToken="${TOKEN}"
  echo "Created github-webhook-secret"
else
  TOKEN="$(oc get secret github-webhook-secret -n "${NS}" -o jsonpath='{.data.secretToken}' | base64 -d)"
  echo "Reusing existing github-webhook-secret"
fi

oc apply -f "${ROOT}/00-namespace-setup.yaml"
oc apply -f "${ROOT}/01-tasks.yaml"
oc apply -f "${ROOT}/02-pipeline.yaml"
oc apply -f "${ROOT}/03-triggers.yaml"

# Wait for EventListener service
echo "Waiting for EventListener service..."
for _ in $(seq 1 60); do
  if oc get svc el-github-listener -n "${NS}" >/dev/null 2>&1; then
    break
  fi
  sleep 2
done
oc apply -f "${ROOT}/03-triggers.yaml" >/dev/null  # ensure Route targets existing Service

HOOK_HOST="$(oc get route el-github-listener -n "${NS}" -o jsonpath='{.spec.host}' 2>/dev/null || true)"
if [[ -z "${HOOK_HOST}" ]]; then
  # Route may need the service first; recreate
  sleep 5
  oc apply -f "${ROOT}/03-triggers.yaml"
  HOOK_HOST="$(oc get route el-github-listener -n "${NS}" -o jsonpath='{.spec.host}')"
fi

echo
echo "=== Shop Tekton CI ready ==="
echo "Namespace:     ${NS}"
echo "App route:     https://$(oc get route rest-service -n "${NS}" -o jsonpath='{.spec.host}')"
echo "Webhook URL:   https://${HOOK_HOST}"
echo "Webhook secret:${TOKEN}"
echo "Domain:        ${DOMAIN}"
echo
echo "Export for GitHub webhook registration:"
echo "  export WEBHOOK_URL=https://${HOOK_HOST}"
echo "  export WEBHOOK_SECRET=${TOKEN}"
