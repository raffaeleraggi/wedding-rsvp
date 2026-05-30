# FRONTEND BUILD
FROM node:22-alpine AS frontend-build

WORKDIR /frontend

COPY frontend/package*.json ./
RUN npm install

COPY frontend .
RUN npm run build


# BACKEND BUILD
FROM maven:3.9.9-eclipse-temurin-21 AS backend-build

WORKDIR /app

COPY backend .

COPY --from=frontend-build /frontend/dist ./src/main/resources/static

RUN mvn -q -DskipTests package


# RUNTIME
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=backend-build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]