FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# 🔒 Cache problemlerini ve modül farklarını önlemek için
COPY . .
RUN mvn -B clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75"

# 🧠 JAR adı / path fark etmez
COPY --from=build /app/target/*.jar app.jar

EXPOSE 7102
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
