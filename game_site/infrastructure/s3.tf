resource "aws_s3_bucket" "site_files" {
  bucket = "er-site-files"
}

module "template_files" {
  source = "hashicorp/dir/template"

  base_dir = "../build"
}

resource "aws_s3_object" "add_files" {
  for_each = module.template_files.files

  bucket       = aws_s3_bucket.site_files.id
  key          = each.key
  source       = each.value.source_path
  content_type = each.value.content_type
  # etag makes the file update when it changes; see https://stackoverflow.com/questions/56107258/terraform-upload-file-to-s3-on-every-apply
  etag = filemd5(each.value.source_path)
}

action "aws_cloudfront_create_invalidation" "invalidation" {
  config {
    distribution_id = aws_cloudfront_distribution.example.id
    paths           = [ for name, file in module.template_files.files : name ]
  }
}

resource "terraform_data" "trigger_invalidation" {
  input = "trigger-invalidation"

  lifecycle {
    action_trigger {
      events  = [before_create, before_update]
      actions = [action.aws_cloudfront_create_invalidation.invalidation]
    }
  }
}
