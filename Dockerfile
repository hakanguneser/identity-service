FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# GitHub Packages credentials for gastroblue-commons (read:packages token).
# Only used in the build stage; the runtime image does not contain them.
ARG GITHUB_ACTOR
ARG GITHUB_TOKEN
RUN test -n "$GITHUB_ACTOR" && test -n "$GITHUB_TOKEN" \
    || (echo "GITHUB_ACTOR and GITHUB_TOKEN build args are required" && exit 1)
RUN mkdir -p /root/.m2 && printf '%s\n' \
    '<settings>' \
    '  <servers>' \
    '    <server>' \
    '      <id>github</id>' \
    "      <username>${GITHUB_ACTOR}</username>" \
    "      <password>${GITHUB_TOKEN}</password>" \
    '    </server>' \
    '  </servers>' \
    '</settings>' > /root/.m2/settings.xml

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
