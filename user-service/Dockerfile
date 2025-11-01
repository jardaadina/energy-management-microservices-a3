FROM maven:3.9 AS build

ENV TZ=UTC
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
ENV CATALINA_OPTS="-Duser.timezone=UTC"

COPY ./pom.xml /user-service/pom.xml
WORKDIR /user-service
RUN mvn dependency:go-offline
COPY ./src/ /user-service/src
WORKDIR /user-service
RUN mvn clean install -DskipTests

FROM openjdk:25-jdk
WORKDIR /user-service
COPY --from=build /user-service/target/user-service-0.0.1-SNAPSHOT.jar /user-service/user-service.jar
EXPOSE 8081
CMD ["java", "-jar", "user-service.jar"]
