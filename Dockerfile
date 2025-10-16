FROM gcr.io/distroless/java21-debian12:debug-nonroot
WORKDIR /app
COPY build/libs/hm-oebs-api-proxy-all.jar app.jar
ENV TZ="Europe/Oslo"
ENV JDK_JAVA_OPTIONS="-Dhttp.proxyHost=webproxy.nais -Dhttps.proxyHost=webproxy.nais -Dhttp.proxyPort=8088 -Dhttps.proxyPort=8088 -Dhttp.nonProxyHosts=localhost|127.0.0.1|10.254.0.1|*.local|*.adeo.no|*.nav.no|*.aetat.no|*.devillo.no|*.oera.no|*.nais.io|*.aivencloud.com|*.intern.dev.nav.no"
CMD ["./app.jar"]
