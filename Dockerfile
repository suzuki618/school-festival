# ビルドステージ
FROM maven:3-eclipse-temurin-21 AS build
COPY . .
RUN mvn clean package -Dmaven.test.skip=true

# 実行ステージ
FROM eclipse-temurin:21-alpine
COPY --from=build /target/SchoolFestival-0.0.1-SNAPSHOT.jar SchoolFestival.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "SchoolFestival.jar"]
