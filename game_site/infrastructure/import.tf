import {
  to = aws_s3_bucket.site_files
  identity = {
    bucket = "er-site-files"
  }
}

import {
  to = aws_s3_bucket_policy.policy
  identity = {
    bucket = "er-site-files"
  }
}

import {
  to = aws_cloudfront_origin_access_control.default
  id = "E9YXJKPVL9U2H"
}

import {
  to = aws_cloudfront_distribution.s3_distribution
  id = "E14BUX1WO7R2RY"
}

import {
  to = aws_s3_object.add_files["todo.html"]
  identity = {
    bucket = "er-site-files"
    key    = "todo.html"
  }
}
import {
  to = aws_s3_object.add_files["style.css"]
  identity = {
    bucket = "er-site-files"
    key    = "style.css"
  }
}
import {
  to = aws_s3_object.add_files["index.html"]
  identity = {
    bucket = "er-site-files"
    key    = "index.html"
  }
}
