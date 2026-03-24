FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# 1️⃣ Sadece pom.xml → dependency cache
COPY pom.xml .
RUN mvn -B dependency:go-offline

# 2️⃣ Source code
COPY src ./src

# 3️⃣ Tek build
RUN mvn -B clean package -DskipTests


FROM eclipse-temurin:21-jre
WORKDIR /app

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75"

COPY --from=build /app/target/*.jar app.jar

EXPOSE 7102
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS  -jar app.jar"]