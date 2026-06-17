# --- Build stage : JDK + Maven pour compiler le jar ---
FROM eclipse-temurin:25-jdk-alpine AS build
WORKDIR /app

# Copie wrapper + pom seuls : couche de dependances en cache tant que pom.xml ne change pas.
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x ./mvnw && ./mvnw dependency:go-offline -B

COPY src ./src
RUN ./mvnw -B -DskipTests package

# --- Runtime stage : JRE seul, image finale minimale (Green Code) ---
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Seul port applicatif PUBLIE a l'hote : la gateway est l'unique point d'entree.
EXPOSE 8080

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
