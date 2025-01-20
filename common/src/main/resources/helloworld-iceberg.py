from pyspark.sql import SparkSession
from pyspark.sql.types import IntegerType, StringType, TimestampType, StructType, StructField


# Initialize Spark session with Iceberg support
spark = (SparkSession.builder
         .appName("Glue-Iceberg-HelloWorld")
         .config("spark.sql.extensions", "org.apache.iceberg.spark.extensions.IcebergSparkSessionExtensions")
         .config("spark.sql.catalog.glue_catalog", "org.apache.iceberg.spark.SparkCatalog")
         .config("spark.sql.catalog.glue_catalog.catalog-impl", "org.apache.iceberg.aws.glue.GlueCatalog")
         .config("spark.sql.catalog.glue_catalog.warehouse", "s3://gluetechadaptertest-target/iceberg/")
         .config("spark.sql.catalog.glue_catalog.io-impl", "org.apache.iceberg.aws.s3.S3FileIO")
         .getOrCreate())

# Define the database and table name
database_name="helloworld"
table_name="users"

# SQL to create the Iceberg table in Glue Catalog
create_table_sql = f"""
CREATE TABLE IF NOT EXISTS glue_catalog.{database_name}.{table_name} (
    id INT,
    name STRING,
    created_at STRING
)
USING iceberg
PARTITIONED BY (created_at)
"""

# Execute SQL to create the table
spark.sql(create_table_sql)

# Sample data
data = [(1, "Alice", "2024-01-01 12:00:00"),
        (2, "Bob", "2024-01-02 12:30:00"),
        (3, "Harry", "2024-01-03 12:30:00"),
        (4, "Hermione", "2024-01-04 12:30:00"),
        (5, "Robin", "2024-01-05 12:30:00"),
        (6, "Bobert", "2024-01-06 12:30:00")]

# Convert data to DataFrame
schema = StructType([
    StructField("id", IntegerType(), False),
    StructField("name", StringType(), False),
    StructField("created_at", StringType(), False)  # Initially as String
])
df = spark.createDataFrame(data, schema)

# Write data to Iceberg table
df.write.format("iceberg").mode("append").save(f"glue_catalog.{database_name}.{table_name}")

print(f"Iceberg table {database_name}.{table_name} created successfully with sample data!")

# Stop Spark session
spark.stop()
