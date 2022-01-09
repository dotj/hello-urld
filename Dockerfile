FROM openjdk:8-jre-alpine
ADD target/scala-2.13/hello-urld-assembly-0.1.0-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]