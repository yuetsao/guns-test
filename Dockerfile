FROM openjdk:8-jdk-alpine
VOLUME /tmp
#EXPOSE 18080
COPY target/*.jar guns_docker.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urand…om","-jar","/guns_docker.jar"]