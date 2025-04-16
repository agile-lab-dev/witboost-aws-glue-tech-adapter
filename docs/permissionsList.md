# AWS permissions

Below is an example of an IAM policy including the permissions to be assigned to the role assumed by the tech adapter in order for it to function properly.

For simplicity, resources are indicated with `"*"`. Restrict the list according to your needs

**Note:**
The placeholders `{accountID}`, `{roleName}`, and `{region}` in the policy below must be replaced with values specific to your AWS setup:
- **`{accountID}`**: Replace this with your AWS account ID.
- **`{roleName}`**: Replace this with the name of the IAM Role that has been created for use with the tech adapter.
- **`{region}`**: Replace this with the AWS region where your resources are located (e.g., `eu-west-1`, `eu-central-1`, etc.).

```json
{
  "Statement": [
    {
      "Action": [
        "s3:PutObject",
        "s3:ListBucket",
        "s3:ListAllMyBuckets",
        "s3:GetObject",
        "s3:GetBucketLocation",
        "s3:DeleteObject",
        "lakeformation:*",
        "kms:TagResource",
        "kms:PutKeyPolicy",
        "kms:GenerateDataKey",
        "kms:Encrypt",
        "kms:Decrypt",
        "glue:getTables",
        "glue:UpdateTable",
        "glue:UpdateJob",
        "glue:ListJobs",
        "glue:GetJob*",
        "glue:GetDatabases",
        "glue:GetDatabase",
        "glue:GetCatalogs",
        "glue:GetCatalog",
        "glue:DeleteTable",
        "glue:DeleteJob",
        "glue:CreateTable",
        "glue:CreateJob",
        "glue:CreateDatabase",
      ],
      "Effect": "Allow",
      "Resource": "*"
    },
    {
      "Action": "iam:PassRole",
      "Effect": "Allow",
      "Resource": "arn:aws:iam::{accountID}:role/{roleName}*"
    },
    {
      "Action": "glue:GetTable",
      "Effect": "Allow",
      "Resource": [
        "arn:aws:glue:{region}:{accountID}:table/*",
        "arn:aws:glue:{region}:{accountID}:database/*",
        "arn:aws:glue:{region}:{accountID}:catalog"
      ]
    }
  ],
  "Version": "2012-10-17"
}
```
