FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75"

COPY --from=build /app/target/identity-service-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 7102
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
