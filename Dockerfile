FROM openjdk:17
ARG JAR_FILE=build/libs/*SNAPSHOT.jar
COPY ${JAR_FILE} app.jar
RUN mkdir -p /var/log/app
ENTRYPOINT ["java", "-jar", "/app.jar"]