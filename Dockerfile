FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# 1️⃣ Sadece pom.xml → dependency cache
COPY pom.xml .
RUN mvn -B dependency:go-offline

# 2️⃣ Source code
COPY src ./src

# 3️⃣ Tek build
RUN mvn -B clean package -DskipTests

# 4️⃣ OpenTelemetry agent (tek sefer)
RUN curl -L \
  https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent.jar \
  -o /app/opentelemetry-javaagent.jar


FROM eclipse-temurin:21-jre
WORKDIR /app

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75"

COPY --from=build /app/target/*.jar app.jar
COPY --from=build /app/opentelemetry-javaagent.jar /app/opentelemetry-javaagent.jar

EXPOSE 7102
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -javaagent:/app/opentelemetry-javaagent.jar -jar app.jar"]