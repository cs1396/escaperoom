resource "aws_s3_bucket" "site_files" {
  bucket = "er-site-files"
}

module "template_files" {
  source = "hashicorp/dir/template"
  base_dir = "../build"
}

# list of current object names
data "aws_s3_objects" "current_keys" {
  bucket = "er-site-files"
}

# data of all current objects
data "aws_s3_object" "current_files" {
  for_each = toset(data.aws_s3_objects.current_keys.keys)

  bucket       = aws_s3_bucket.site_files.id
  key          = each.key
}

locals {
  # md5 of remote objects
  remote_tags = { for name, file in data.aws_s3_object.current_files : name => file.etag }
}


resource "aws_s3_object" "add_files" {
  for_each = module.template_files.files
  # always wait to do this until after current file md5s are read
  depends_on = [ data.aws_s3_object.current_files ]

  bucket       = aws_s3_bucket.site_files.id
  key          = each.key
  source       = each.value.source_path
  content_type = each.value.content_type

  # etag makes the file update when it changes; see https://stackoverflow.com/questions/56107258/terraform-upload-file-to-s3-on-every-apply
  etag = filemd5(each.value.source_path)
}

locals {
  # files changed or added
  changes = compact([ for name, file in resource.aws_s3_object.add_files : lookup(local.remote_tags, name, "") != file.etag ? "/${name}" : null])
}

action "aws_cloudfront_create_invalidation" "invalidation" {
  config {
    distribution_id = aws_cloudfront_distribution.s3_distribution.id
    paths           = local.changes
  }
}

resource "terraform_data" "trigger_invalidation" {
  count = length(local.changes) > 0 ? 1 : 0
  input = "trigger-invalidation"

  lifecycle {
    action_trigger {
      events  = [before_create, before_update]
      actions = [action.aws_cloudfront_create_invalidation.invalidation]
    }
  }
}
