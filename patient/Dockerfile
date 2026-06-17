# --- Build stage : JDK + Maven pour compiler le jar ---
FROM eclipse-temurin:25-jdk-alpine AS build
WORKDIR /app

# Copie d'abord le wrapper + pom seuls : tant que pom.xml ne change pas, la couche
# de telechargement des dependances reste en cache (build incremental plus rapide / sobre).
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x ./mvnw && ./mvnw dependency:go-offline -B

# Puis le code source : seule cette couche (et les suivantes) se reconstruit sur un changement de src.
COPY src ./src
RUN ./mvnw -B -DskipTests package

# --- Runtime stage : JRE seul, image finale minimale (Green Code) ---
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Port interne du microservice (non publie a l'hote : accessible via la gateway uniquement).
EXPOSE 8081

# MaxRAMPercentage : la JVM dimensionne son heap sur la RAM ALLOUEE AU CONTENEUR
# (et non sur la RAM de l'hote) -> evite l'OOM kill en environnement contraint.
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
