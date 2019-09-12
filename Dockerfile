FROM openjdk:8-slim

ENV JAVA_OPTS="-XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap"

ADD target/db-operator-*.jar /db-operator.jar

CMD ["java", "-jar", "/db-operator.jar"]
