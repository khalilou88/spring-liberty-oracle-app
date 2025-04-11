# Oracle Database Docker Setup Guide

This guide walks through setting up an Oracle database in Docker to match the following JDBC configuration:

```xml
<dataSource id="OracleDataSource" jndiName="jdbc/myOracleDS">
    <jdbcDriver libraryRef="OracleLib" />
    <properties.oracle URL="jdbc:oracle:thin:@//localhost:1521/ORCLPDB1"
                       user="scott"
                       password="tiger" />
    <connectionManager maxPoolSize="50" minPoolSize="10" />
</dataSource>
```

## Prerequisites

- Docker installed on your system
- Oracle account (for downloading the official image)

## Step 1: Pull the Oracle Database Image

You'll need to login to the Oracle Container Registry to download the image:

```bash
docker login container-registry.oracle.com
docker pull container-registry.oracle.com/database/enterprise:latest
```

## Step 2: Create and Run the Oracle Container

Run the following command to create an Oracle database container:

```bash
docker run -d --name oracle \
  -p 1521:1521 -p 5500:5500 \
  -e ORACLE_PWD=tiger \
  -e ORACLE_SID=ORCLCDB \
  -e ORACLE_PDB=ORCLPDB1 \
  -e ORACLE_CHARACTERSET=AL32UTF8 \
  -v oracle-data:/opt/oracle/oradata \
  container-registry.oracle.com/database/enterprise:latest
```

In PowerShell, use backticks (`) for line continuation:
```powerShell
docker run -d --name oracle `
  -p 1521:1521 -p 5500:5500 `
  -e ORACLE_PWD=tiger `
  -e ORACLE_SID=ORCLCDB `
  -e ORACLE_PDB=ORCLPDB1 `
  -e ORACLE_CHARACTERSET=AL32UTF8 `
  -v oracle-data:/opt/oracle/oradata `
  container-registry.oracle.com/database/enterprise:latest
```

This command:
- Creates a container named "oracle"
- Maps ports 1521 (database) and 5500 (Enterprise Manager Express)
- Sets the password to "tiger"
- Creates a database with SID "ORCLCDB"
- Creates a PDB named "ORCLPDB1" (matches the connection string)
- Persists database files to a volume

## Step 3: Create the 'scott' User

Wait a few minutes for the container to initialize, then:

```bash
# Connect to the container
docker exec -it oracle bash

# Connect to Oracle as sysdba
sqlplus / as sysdba

# Create scott user and grant permissions
ALTER SESSION SET CONTAINER=ORCLPDB1;
CREATE USER scott IDENTIFIED BY tiger;
GRANT CONNECT, RESOURCE, DBA TO scott;
EXIT;
```

## Step 4: Verify Connection

You can verify the connection from within the container:

```bash
# Connect to the container
docker exec -it oracle bash

# Connect as scott
sqlplus scott/tiger@//localhost:1521/ORCLPDB1
```

## Managing Your Oracle Container

```bash
# Stop the container
docker stop oracle

# Start the container
docker start oracle

# Remove the container (will lose data if not using volumes)
docker rm oracle

# Check container logs
docker logs oracle
```

## Notes

- Initial database setup may take several minutes
- If experiencing issues, check the container logs with `docker logs oracle`
- For production use, consider additional security measures and proper password management
