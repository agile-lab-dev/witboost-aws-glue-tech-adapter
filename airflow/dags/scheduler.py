"""Sample DAG"""
import airflow.utils
from airflow.providers.amazon.aws.operators.glue import GlueJobOperator
from airflow import DAG
from datetime import timedelta
import airflow.utils

# allow backfills via DAG run parameters
process_date = '{{ dag_run.conf.get("process_date") if dag_run.conf.get("process_date") else "NONE" }}'

dag = DAG(
    dag_id = "Airflow-Glue.Test0",
    default_args = {
        'depends_on_past':False,
        'start_date':airflow.utils.dates.days_ago(0),
        'retries':1,
        'retry_delay':timedelta(minutes=5),
        'catchup': False
    },
    schedule_interval = None, # None for unscheduled or a cron expression - E.G. "00 12 * * 2" - at 12noon Tuesday
    dagrun_timeout = timedelta(minutes=5),
    max_active_runs = 1,
    max_active_tasks = 1 # since there is only one task in our DAG
)

## Log ingest. Assumes Glue Job is already created
glue_ingestion_job = GlueJobOperator(
    task_id="test-af-run-0",
    job_name="test-0",
    script_args={
    },
    region_name="eu-central-1",
    dag=dag,
    verbose=True
)

glue_ingestion_job