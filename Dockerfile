FROM openjdk:12
VOLUME /tmp
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} Usuarios.jar
ENTRYPOINT ["java","-jar","/Usuarios.jar"]