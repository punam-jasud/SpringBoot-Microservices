FROM openjdk:17-oracle

ARG JAR_FILE=target/*.jar

COPY ${JAR_FILE} ServiceRegistry.jar

ENTRYPOINT [ "java", "-jar", "/ServiceRegistry.jar"]

EXPOSE 8761