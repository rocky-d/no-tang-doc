resource "kubernetes_namespace" "keycloak" {
  metadata { name = "keycloak" }
}

locals {
  db = data.terraform_remote_state.database.outputs.keycloak_db_connection
}

resource "kubernetes_secret" "keycloak_db" {
  metadata {
    name      = "keycloak-db-secret"
    namespace = kubernetes_namespace.keycloak.metadata[0].name
  }
  type = "Opaque"
  data = { password = local.db.password }
}

resource "helm_release" "keycloak" {
  name             = "keycloak"
  repository       = "oci://registry-1.docker.io/bitnamicharts"
  chart            = "keycloak"
  namespace        = kubernetes_namespace.keycloak.metadata[0].name
  create_namespace = false
  version          = "25.2.0"
  timeout = 1200



  values = [yamlencode({

    proxyHeaders = "xforwarded"    # 信任 Ingress 传入的 X-Forwarded-* 头

    auth = {
      adminUser     = "ntdoc-admin"
      adminPassword = var.kc_admin_password
    }

    production = true
    extraEnvVars = [
      { name = "KC_PROXY", value = "edge" },
      { name = "KC_HOSTNAME", value = "auth.ntdoc.site" },
      { name = "KC_DB_URL_PROPERTIES", value = "sslmode=require" }
    ]

    externalDatabase = {
      host                      = local.db.host
      port                      = local.db.port
      user                      = "doadmin"
      database                  = local.db.database
      existingSecret            = "keycloak-db-secret"
      existingSecretPasswordKey = "password"
    }
    image = {
      registry   = "docker.io"
      repository = "bitnamilegacy/keycloak"
      tag        = "26.3.3-debian-12-r0"
      pullPolicy = "IfNotPresent"
    }
    service = { type = "ClusterIP", ports = { http = 8080 } }
    postgresql = {
      enabled = false
    }
    ingress = {
      enabled          = true
      ingressClassName = "nginx"
      hostname         = "auth.ntdoc.site"
      tls              = true
      annotations = {
        "cert-manager.io/cluster-issuer" = "letsencrypt"
        "acme.cert-manager.io/http01-edit-in-place" = "true"
      }
      # extraTls = [{
      #   hosts      = ["auth.ntdoc.site"]
      #   secretName = "keycloak-tls"
      # }]
    }
    customStartupProbe = {
      httpGet = {
        path = "/realms/master"
        port = "http"
        scheme = "HTTP"
      }
      initialDelaySeconds = 120
      periodSeconds       = 10
      timeoutSeconds      = 5
      failureThreshold    = 60
    }
    readinessProbe = {
      enabled             = true
      initialDelaySeconds = 90
      periodSeconds       = 10
      timeoutSeconds      = 5
      failureThreshold    = 12
    }
    livenessProbe = {
      enabled             = true
      initialDelaySeconds = 300  # 等 Keycloak 完全起来再开始活性检查
      periodSeconds       = 20
      timeoutSeconds      = 5
      failureThreshold    = 6
    }
    resources = {
      requests = { cpu = "500m",  memory = "1Gi" }
      limits   = { cpu = "1000m", memory = "2Gi" }
    }
  })]
}
