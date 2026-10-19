FROM eclipse-temurin:21-jdk AS build

WORKDIR /build

COPY . .

RUN chmod +x mvnw \
    && ./mvnw clean package -DskipTests


FROM eclipse-temurin:21-jre

WORKDIR /app

RUN groupadd --gid 10001 campuslab \
    && useradd \
        --uid 10001 \
        --gid campuslab \
        --no-create-home \
        --shell /usr/sbin/nologin \
        campuslab

COPY --from=build \
    --chown=10001:10001 \
    /build/target/ms-campuslab-rabbit-admin-0.0.1-SNAPSHOT.jar \
    /app/app.jar

ENV JAVA_TOOL_OPTIONS="-Xms32m -Xmx128m -XX:MaxMetaspaceSize=128m -XX:+ExitOnOutOfMemoryError"

USER 10001:10001

EXPOSE 8084

ENTRYPOINT ["java", "-jar", "/app/app.jar"]