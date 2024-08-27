FROM azul/zulu-openjdk-alpine:21 AS builder
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} application.jar
COPY docker-entrypoint.sh .
RUN java -Djarmode=tools -jar application.jar extract --layers --launcher

FROM azul/zulu-openjdk-alpine:21-jre-headless
#RUN apt-get -qq update && \
#    apt-get -qqy install curl
COPY --from=builder application/dependencies/ ./
COPY --from=builder application/snapshot-dependencies/ ./
COPY --from=builder application/spring-boot-loader/ ./
COPY --from=builder application/application/ ./
COPY --from=builder docker-entrypoint.sh ./
#ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
ENTRYPOINT ["sh", "./docker-entrypoint.sh"]