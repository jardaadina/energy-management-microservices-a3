FROM maven:3.9-amazoncorretto-25 AS build
ENV TZ=UTC
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
ENV CATALINA_OPTS="-Duser.timezone=UTC"

COPY ./pom.xml /customer-support-service/pom.xml
WORKDIR /customer-support-service
RUN mvn dependency:go-offline
COPY ./src/ /customer-support-service/src
WORKDIR /customer-support-service
RUN mvn clean install -DskipTests

FROM amazoncorretto:25
WORKDIR /customer-support-service
COPY --from=build /customer-support-service/target/customer-support-service-0.0.1-SNAPSHOT.jar /customer-support-service/customer-support-service.jar
EXPOSE 8081
CMD ["java", "-jar", "customer-support-service.jar"]
