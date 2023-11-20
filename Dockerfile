FROM gcr.io/distroless/java17-debian12:latest
COPY /build/libs/hm-oebs-api-proxy-1.0-SNAPSHOT.jar /app.jar
COPY init.sh /init.sh
CMD ["/init.sh"]
