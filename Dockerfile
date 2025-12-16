FROM amazoncorretto:21-alpine

WORKDIR /app

COPY gradlew .
COPY gradle/wrapper/gradle-wrapper.properties gradle/wrapper/
COPY gradle/wrapper/gradle-wrapper.jar gradle/wrapper/

COPY build.gradle .
COPY settings.gradle .
COPY src ./src

RUN chmod +x gradlew && ./gradlew bootJar -x test

ENTRYPOINT ["java", "-jar", "build/libs/my-blog-back-app-0.0.1-SNAPSHOT.jar"]