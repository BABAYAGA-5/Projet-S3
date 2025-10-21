# Multi-stage build: build WAR with Maven, then run on Tomcat 10.1 (Jakarta EE 10)

FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests package

FROM tomcat:10.1-jdk21-temurin
LABEL maintainer="you@example.com"

# Remove default ROOT to deploy ours
RUN rm -rf /usr/local/tomcat/webapps/*

# Deploy built WAR as ROOT.war so it serves at '/'
COPY --from=build /app/target/*.war /usr/local/tomcat/webapps/ROOT.war

EXPOSE 8080

# Basic healthcheck (optional); comment if causing issues
# HEALTHCHECK --interval=30s --timeout=3s CMD wget -qO- http://localhost:8080/ || exit 1

CMD ["catalina.sh", "run"]
