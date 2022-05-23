FROM openjdk:11-jdk-slim
COPY "./target/CreditsService-0.0.1-SNAPSHOT.jar" "app.jar"
EXPOSE 8083 
ENTRYPOINT ["java","-jar","app.jar"]