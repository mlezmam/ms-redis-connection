FROM us-docker.pkg.dev/sit-cicd-images-base/liverpool-openjdk-images-base/openjdk:21-slim

# Instalamos openssl para convertir el pem (using wildcard to avoid potential version issues in coming v3.0.*)
RUN apt-get update && apt-get install -y --no-install-recommends 'openssl=3.0.*' && rm -rf /var/lib/apt/lists/*

# Set working dir
WORKDIR /app

# Copy the .pem certificate
COPY redis-cert.pem /etc/ssl/certs/redis-cert.pem

# Convert .pem to .crt (X.509 format) and import into Java truststore
RUN openssl x509 -outform der -in /etc/ssl/certs/redis-cert.pem -out /etc/ssl/certs/redis-cert.der && \
    keytool -import -alias redis_cert -keystore "$JAVA_HOME/lib/security/cacerts" \
    -storepass changeit -file /etc/ssl/certs/redis-cert.der -noprompt

# Copy jar
COPY target/ms-plp-search.jar app.jar

# Set environment profile
ARG ENVIRONMENT
ENV ENVIRONMENT=$ENVIRONMENT

EXPOSE 8080

# Run application
ENTRYPOINT ["sh", "-c", "java -Dspring.profiles.active=$ENVIRONMENT -jar app.jar"]
