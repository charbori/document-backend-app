FROM amazoncorretto:17.0.7-alpine

RUN whoami

# Add app user
ARG APPLICATION_USER=ubuntu
RUN adduser --no-create-home -u 1001 -D $APPLICATION_USER

# Configure working directory
RUN mkdir /app && \
    mkdir /app/videos && \
    mkdir /app/tus && \
    chown -R $APPLICATION_USER /app

USER 1001

COPY --chown=1001:1001 ./build/libs/web-differ.jar /app/app.jar
WORKDIR /app

EXPOSE 8888
ENTRYPOINT [ "java", "-jar", "-Dspring.profiles.active=prod", "/app/app.jar" ]