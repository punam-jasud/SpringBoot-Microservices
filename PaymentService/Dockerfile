FROM openjdk:17-oracle

ENV MYSQL_HOST=192.168.1.2
ENV MYSQL_PORT=3306
ENV MYSQL_USERNAME=root
ENV MYSQL_PASSWORD=root
ENV CONFIG_SERVER_URL=host.docker.internal
ENV EUREKA_SERVER_ADDRESS=http://host.docker.internal:8761/eureka

ARG JAR_FILE=target/*.jar

COPY ${JAR_FILE} PaymentService.jar

CMD mysqld

ENTRYPOINT [ "java", "-jar", "/PaymentService.jar"]

EXPOSE 8082