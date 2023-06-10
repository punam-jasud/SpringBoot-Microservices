FROM openjdk:17-oracle

ARG JAR_FILE=target/*.jar

COPY ${JAR_FILE} ConfigServer.jar

ENTRYPOINT [ "java", "-jar", "/ConfigServer.jar"]

EXPOSE 9296