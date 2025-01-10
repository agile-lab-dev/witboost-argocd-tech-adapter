# High Level Design

This document describes the Hig h Level Design of the ArgoCD Tech Adapter.
The source diagrams can be found and edited in the [accompanying draw.io file](hld.drawio).

- [Overview](#overview)
- [Provisioning](#provisioning)
- [Unprovisioning](#unprovisioning)


## Overview

### Tech Adapter

A Tech Adapter (TA) is a service in charge of performing a resource allocation task, usually
through a Cloud Provider. The resources to allocate are typically referred to as the _Component_, the
details of which are described in a YAML file, known as _Component Descriptor_.

The TA is invoked by an upstream service of the Witboost platform, namely the Coordinator, which is in charge of orchestrating the creation
of a complex infrastructure by coordinating several TAs in a single workflow. The TA receives
the _Data Product Descriptor_ as input with all the components (because it might need more context) plus the id of the component to provision, named _componentIdToProvision_

To enable the above orchestration a TA exposes an API made up of five main operations:
- validate: checks if the provided component descriptor is valid and reports any errors
- provision: allocates resources based on the previously validated descriptor; clients either receive an immediate response (synchronous) or a token to monitor the provisioning process (asynchronous)
- status: for asynchronous provisioning, provides the current status of a provisioning request using the provided token
- unprovision: destroys the resources previously allocated.
- updateacl: grants access to a specific component/resource to a list of users/groups

### ArgoCD Tech Adapter

This Tech Adapter provides integration with ArgoCD to manage GitOps workflows. The components are Workload based on an ArgoCD Application.

It offers:
- Repository management: Create and update repositories.
- Project management: Create and update projects with source repositories and destinations.
- Application lifecycle management: Create, update, delete applications, and retrieve their statuses.


## Provisioning
![HLD_provisioning.png](img%2FHLD_provisioning.png)

This flow enables the creation and management of projects, repositories, and applications in an idempotent manner.

#### - **Provisioning Request**
- A provisioning request is sent to the system by the **Provisioning Coordinator**.
- This request includes metadata required to configure projects, repositories, and applications in ArgoCD.

#### - **Request Validation**
- The **ArgoCD Tech Adapter** validates the request to ensure it is complete and correct.
- Mandatory fields and data consistency are verified.

#### - **Metadata Extraction**
- Descriptive metadata is extracted from the request to identify the resources to be created or updated.

#### -  **Project Creation/Update**
- If the specified project does not exist in ArgoCD, it is created.
- If it already exists, the project is updated to include new configurations such as source repositories and destinations.

#### - **Repository Creation/Update**
ArgoCD is configured to pull manifests from the component's Git repository.
This process includes specifying:
- Repository URL: The Git repository's address.
- Git Access Credentials: An access token, securely stored in the tech-adapter configuration, is used to authenticate and authorize ArgoCD to access the repository.


#### - **Application Creation**
- Using the *upsert* mode (create or update), the application is configured in ArgoCD.
- The configuration includes code sources and Kubernetes destinations.

#### - **Resource Synchronization**
- All application resources are synchronized to the target Kubernetes cluster.

#### - **Operation Result Reporting**
- The final status of the operation is reported as the outcome of the provisioning process.
- This includes the status of the application in case of success or errors that have been raised.

### Requirements

- **Technical User**: A technical user with proper permissions is required to manage projects, repositories, and applications in ArgoCD.
- **Kubernetes Access**: Applications are deployed to target Kubernetes clusters as defined in the configuration.


## Unprovisioning

![HLD_unprovisioning.png](img%2FHLD_unprovisioning.png)

Unprovisioning consists of removing the existing application associated to the components. The ArgoCD repositories and projects are never deleted, as other components may still rely on it, and we prefer to avoid their accidental deletion. The tech adapter can easily be extended to do this in case automated deletion is preferred.

#### - **Unprovisioning Request**
- An unprovisioning request is sent to the system by the **Provisioning Coordinator**.
- This request includes metadata required to remove applications in ArgoCD.

#### - **Request Validation**
- The **ArgoCD Tech Adapter** validates the request to ensure it is complete and correct.
- Mandatory fields and data consistency are verified.

#### - **Metadata Extraction**
- Relevant metadata is extracted from the request to identify the target application for removal.

#### - **Application Deletion**
- The specified application is deleted from ArgoCD.
- The process is idempotent, ensuring that no errors occur if the application has already been removed.


#### - **Operation Result Reporting**
- The final status of the operation is reported as the outcome of the unprovisioning process.
- In case of success, confirmation is provided, otherwise errors are logged and reported.


### Requirements

- **Technical User**: A technical user with sufficient permissions to manage applications in ArgoCD is required.


