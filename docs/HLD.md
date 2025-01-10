# High Level Design
This document describes the High Level Design of a module that handles AWS Glue Jobs using the AWS SDK.

## Overview

### Tech Adapter
A Tech Adapter (TA) is a service in charge of performing a resource allocation task, usually through a Cloud Provider. The resources to allocate are typically referred to as the Component, the details of which are described in a YAML file, known as Component Descriptor.
The TA is invoked by an upstream service of the Witboost platform, namely the Coordinator, which is in charge of orchestrating the creation of a complex infrastructure by coordinating several SPs in a single workflow. The TA receives the Data Product Descriptor as input with all the components (because it might need more context) plus the id of the component to provision, named componentIdToProvision
To enable the above orchestration a TA exposes an API made up of five main operations:

- validate: checks if the provided component descriptor is valid and reports any errors
- provision: allocates resources based on the previously validated descriptor; clients either receive an immediate response (synchronous) or a token to monitor the provisioning process (asynchronous)
- status: for asynchronous provisioning, provides the current status of a provisioning request using the provided token
- unprovision: destroys the resources previously allocated.

### AWS Glue
AWS Glue is a serverless data integration service that makes it easy for analytics users to discover, prepare, move, and integrate data from multiple sources. You can use it for analytics, machine learning, and application development. 
It also includes additional productivity and data ops tooling for authoring, running jobs, and implementing business workflows.

### AWS Glue Jobs Tech Adapter
AWS Glue Jobs Tech Adapter is used to provision an AWS Glue job. The tech adapter only creates the Job, but it will not run it. The run or the schedule of the job is meant to be accountability of a scheduler.
The logic of the job is contained in a script file placed in a S3 location, stored there by the CI/CD of the Glue Template.



#### Validate
This tech adapter interacts with Glue creating a Glue Job, so the validation checks that the descriptor contains the values needed to properly create the job definition.
This step must also check that the script file is existing in the defined S3 location.

#### Provision
The provisioning phase creates the Job if it doesn't exist, or it updates it.

![HLD-Glue Job - provisioning.png](img/HLD-Glue%20Job%20-%20provisioning.png)

**Naming Convention**

`Job name: job-<domain>-<dpname>-<env>-<majversion>-<compname>`

**N.B.** Env could be avoided if each env has its own AWS accounts

#### Unprovision
During the unprovisioning phase, the tech adapter removes the job

![HLD-Glue Job - unprovisioning.png](img/HLD-Glue%20Job%20-%20unprovisioning.png)
