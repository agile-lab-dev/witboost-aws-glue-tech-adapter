# Airflow test environment

This folder contains a basic configuration for running airflow in docker.

The `dags` folder contains example schedulers to test the interaction between airflow and Glue.

### AWS authentication

In order for your DAG to correctly interact with AWS, your worker process must be authenticated. For that, you can add the three aws env vars to the 
`services.airflow-worker.environment`.

```yaml
services:
    #...
    airflow-worker:
        #...
        environment:
            #...
            AWS_ACCESS_KEY_ID: ""
            AWS_SECRET_ACCESS_KEY: ""
            AWS_SESSION_TOKEN: ""
```