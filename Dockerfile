FROM navikt/java:17
COPY init.sh /init-scripts/init.sh
COPY build/libs/hm-oebs-api-proxy-1.0-SNAPSHOT.jar app.jar
