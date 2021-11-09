FROM navikt/java:15
COPY init.sh /init-scripts/init.sh
COPY /build/libs/hm-oebs-api-proxy-1.0-SNAPSHOT-all.jar app.jar
