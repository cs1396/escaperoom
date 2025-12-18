# See https://docs.aws.amazon.com/AmazonCloudFront/latest/DeveloperGuide/private-content-restricting-access-to-s3.html
data "aws_iam_policy_document" "origin_bucket_policy" {
  statement {
    sid    = "AllowCloudFrontServicePrincipalReadWrite"
    effect = "Allow"

    principals {
      type        = "Service"
      identifiers = ["cloudfront.amazonaws.com"]
    }

    actions = [
      "s3:GetObject",
      "s3:PutObject",
    ]

    resources = [
      "${aws_s3_bucket.site_files.arn}/*",
    ]

    condition {
      test     = "StringEquals"
      variable = "AWS:SourceArn"
      values   = [aws_cloudfront_distribution.s3_distribution.arn]
    }
  }
}

resource "aws_s3_bucket_policy" "policy" {
  bucket = aws_s3_bucket.site_files.bucket
  policy = data.aws_iam_policy_document.origin_bucket_policy.json
}

locals {
  s3_origin_id = "Escaperoom Origin"
  # my_domain    = "uaqapps.com"
}

# data "aws_acm_certificate" "uaqapps" {
#   region   = "us-east-1"
#   domain   = "*.${local.my_domain}"
#   statuses = ["ISSUED"]
# }

resource "aws_cloudfront_origin_access_control" "default" {
  name                              = "default-oac"
  origin_access_control_origin_type = "s3"
  signing_behavior                  = "always"
  signing_protocol                  = "sigv4"
}

resource "aws_cloudfront_distribution" "s3_distribution" {
  origin {
    domain_name = aws_s3_bucket.site_files.bucket_regional_domain_name
    origin_access_control_id = aws_cloudfront_origin_access_control.default.id
    origin_id = local.s3_origin_id
  }

  enabled             = true
  is_ipv6_enabled     = true
  default_root_object = "index.html"

  web_acl_id = "arn:aws:wafv2:us-east-1:442426877818:global/webacl/CreatedByCloudFront-629d68d0/6854365c-4f7e-409d-a656-7b2e122dafe9"

  # aliases = ["escaperoom.${local.my_domain}"]

  default_cache_behavior {
    allowed_methods  = ["GET", "HEAD"]
    cached_methods   = ["GET", "HEAD"]

    target_origin_id = local.s3_origin_id
    compress = true

    cache_policy_id = "658327ea-f89d-4fab-a63d-7e88639e58f6" # default `CachingOptimized`
    viewer_protocol_policy = "redirect-to-https"
  }
  restrictions {
    geo_restriction {
      restriction_type = "whitelist"
    }
  }

  viewer_certificate {
    cloudfront_default_certificate = true
    # acm_certificate_arn = data.aws_acm_certificate.uaqapps.arn
    # ssl_support_method  = "sni-only"
  }
}

# # Create Route53 records for the CloudFront distribution aliases
# data "aws_route53_zone" "uaqapps" {
#   name = local.my_domain
# }

# resource "aws_route53_record" "cloudfront" {
#   for_each = aws_cloudfront_distribution.s3_distribution.aliases
#   zone_id  = data.aws_route53_zone.uaqapps.zone_id
#   name     = each.value
#   type     = "A"
#
#   alias {
#     name                   = aws_cloudfront_distribution.s3_distribution.domain_name
#     zone_id                = aws_cloudfront_distribution.s3_distribution.hosted_zone_id
#     evaluate_target_health = false
#   }
# }
