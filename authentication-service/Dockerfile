FROM maven:3.9 AS build

ENV TZ=UTC
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
ENV CATALINA_OPTS="-Duser.timezone=UTC"

COPY ./pom.xml /authentication-service/pom.xml
WORKDIR /authentication-service
RUN mvn dependency:go-offline
COPY ./src/ /authentication-service/src
WORKDIR /authentication-service
RUN mvn clean install -DskipTests

FROM openjdk:25-jdk
WORKDIR /authentication-service
COPY --from=build /authentication-service/target/authentication-service-0.0.1-SNAPSHOT.jar /authentication-service/authentication-service.jar
EXPOSE 8083
CMD ["java", "-jar", "authentication-service.jar"]
