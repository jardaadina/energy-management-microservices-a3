FROM maven:3.9-amazoncorretto-25 AS build
ENV TZ=UTC
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
ENV CATALINA_OPTS="-Duser.timezone=UTC"

COPY ./pom.xml /load-balancer-service/pom.xml
WORKDIR /load-balancer-service
RUN mvn dependency:go-offline
COPY ./src/ /load-balancer-service/src
WORKDIR /load-balancer-service
RUN mvn clean install -DskipTests

FROM amazoncorretto:25
WORKDIR /load-balancer-service
COPY --from=build /load-balancer-service/target/load-balancer-service-0.0.1-SNAPSHOT.jar /load-balancer-service/load-balancer-service.jar
EXPOSE 8081
CMD ["java", "-jar", "load-balancer-service.jar"]
