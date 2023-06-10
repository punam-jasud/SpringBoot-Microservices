FROM openjdk:17-oracle

ARG JAR_FILE=target/*.jar

COPY ${JAR_FILE} CloudGateway.jar

ENTRYPOINT [ "java", "-jar", "/CloudGateway.jar"]

EXPOSE 9090