provider "azurerm" {
  version = "1.19.0"
}

locals {
  app = "repl-docmosis-backend"
  create_api = "${var.env != "preview" && var.env != "spreview"}"

  previewVaultName = "${var.product}-aat"
  nonPreviewVaultName = "${var.product}-${var.env}"
  vaultName = "${var.env == "preview" ? local.previewVaultName : local.nonPreviewVaultName}"
  vaultUri = "${data.azurerm_key_vault.ethos_key_vault.vault_uri}"
  previewVaultGroupName = "${var.product}-${local.app}-aat"
  nonPreviewVaultGroupName = "${var.product}-${local.app}-${var.env}"
  vaultGroupName = "${var.env == "preview" ? local.previewVaultGroupName : local.nonPreviewVaultGroupName}"

  # list of the thumbprints of the SSL certificates that should be accepted by the API (gateway)
//  allowed_certificate_thumbprints = [
//    # API tests
//    "${var.api_gateway_test_certificate_thumbprint}"
//  ]
//
//  thumbprints_in_quotes = "${formatlist("&quot;%s&quot;", local.allowed_certificate_thumbprints)}"
//  thumbprints_in_quotes_str = "${join(",", local.thumbprints_in_quotes)}"
//  api_policy = "${replace(file("template/api-policy.xml"), "ALLOWED_CERTIFICATE_THUMBPRINTS", local.thumbprints_in_quotes_str)}"
//  api_base_path = "ethos-repl-docmosis-service"
}

module "repl-docmosis-backend" {
  source                          = "git@github.com:hmcts/moj-module-webapp.git"
  product                         = "${var.product}-${local.app}"
  location                        = "${var.location}"
  env                             = "${var.env}"
  ilbIp                           = "${var.ilbIp}"
  subscription                    = "${var.subscription}"
  capacity                        = "${var.capacity}"
  common_tags                     = "${var.common_tags}"
  appinsights_instrumentation_key = "${var.appinsights_instrumentation_key}"
  is_frontend                     = false

  app_settings                         = {
    WEBSITE_PROACTIVE_AUTOHEAL_ENABLED = "${var.autoheal}"
    TORNADO_URL                        = "${var.tornado_url}"
    TORNADO_ACCESS_KEY                 = "${var.tornado_access_key}"
    IDAM_USER_BASE_URI                 = "${var.idam_api_url}"
    //CCD_DATA_STORE_API_URL             = "${var.ccd_data_store_api_url}"
    DOCUMENT_MANAGEMENT_URL            = "${var.dm_url}"
    DOCUMENT_MANAGEMENT_CASEWORKERROLE = "caseworker-ethos"
    SERVICE_AUTH_PROVIDER_URL          = "${var.s2s_url}"
  }
}

data "azurerm_key_vault" "ethos_key_vault" {
  name                = "${local.vaultGroupName}"
  resource_group_name = "${local.vaultGroupName}"
}

//module "key-vault" {
//  source                  = "git@github.com:hmcts/cnp-module-key-vault?ref=master"
//  product                 = "${var.product}"
//  env                     = "${var.env}"
//  tenant_id               = "${var.tenant_id}"
//  object_id               = "${var.jenkins_AAD_objectId}"
//  resource_group_name     = "${module.repl-docmosis-backend.resource_group_name}"
//  # dcd_cc-dev group object ID
//  product_group_object_id = "38f9dea6-e861-4a50-9e73-21e64f563537"
//  common_tags             = "${var.common_tags}"
//}

# region API (gateway)

//data "template_file" "api_template" {
//  template = "${file("${path.module}/template/api.json")}"
//}
//
//resource "azurerm_template_deployment" "api" {
//  template_body       = "${data.template_file.api_template.rendered}"
//  name                = "${var.product}-api-${var.env}"
//  deployment_mode     = "Incremental"
//  resource_group_name = "core-infra-${var.env}"
//  count               = "${local.create_api ? 1 : 0}"
//
//  parameters = {
//    apiManagementServiceName  = "core-api-mgmt-${var.env}"
//    apiName                   = "ethos-repl-docmosis-service"
//    apiProductName            = "ethos-repl-docmosis"
//    serviceUrl                = "http://${var.product}-${local.app}-${var.env}.service.core-compute-${var.env}.internal"
//    apiBasePath               = "${local.api_base_path}"
//    policy                    = "${local.api_policy}"
//  }
//}
