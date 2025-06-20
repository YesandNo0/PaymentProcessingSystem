FROM maven:3.9.9-amazoncorretto-21-debian AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -e -X -DskipTests

FROM amazoncorretto:21
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]