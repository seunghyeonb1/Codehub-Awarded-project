# CLH Bookstore Application

## Description

The CLH Bookstore Application is a key component of the CLH infrastructure, designed to manage and store data
efficiently. This application is built using Java 17 and is containerized using Docker, ensuring ease of deployment and
scalability.

## Environment Variables

The application requires the following environment variables to be set:

- `POSTGRES_URL`: URL for the PostgreSQL database (e.g., `jdbc:postgresql://<hostname>/<dbname>`)
- `POSTGRES_USER`: Username for the PostgreSQL database
- `POSTGRES_PASSWORD`: Password for the PostgreSQL database

## Building the Application

To build the application, you can use the provided Gradle wrapper. Ensure that you have Java 17 installed on your
system. Run the following command in the root directory of the project:

```bash
./gradlew build
```

This command compiles the application and generates the necessary artifacts.

## Launching the Application

For launching the application, refer to the Docker Compose setup available
at [CLH Deployment Repository](https://github.com/comp313-005-team2-w24/clh-deployment). The Docker Compose file is
configured to set up the application along with its dependencies, such as PostgreSQL and Redis.

## License

This project is licensed under the terms of the [LICENSE](LICENSE) file in the root directory of this project. Please
refer to the file for detailed information on the licensing terms and conditions.