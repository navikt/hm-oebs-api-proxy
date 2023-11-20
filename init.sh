if [ -d "/secrets/oebst1" ]; then
    export HM_OEBS_API_PROXY_DB_USR_T1=$(cat /secrets/oebst1/credentials/username)
    export HM_OEBS_API_PROXY_DB_PW_T1=$(cat /secrets/oebst1/credentials/password)
    export HM_OEBS_API_PROXY_DB_URL_T1=jdbc:oracle:thin:@d26dbfl027.test.local:1521/oebst1
fi

if [ -d "/secrets/oebsq1" ]; then
    export HM_OEBS_API_PROXY_DB_USR_Q1=$(cat /secrets/oebsq1/credentials/username)
    export HM_OEBS_API_PROXY_DB_PW_Q1=$(cat /secrets/oebsq1/credentials/password)
    export HM_OEBS_API_PROXY_DB_URL_Q1=$(cat /secrets/oebsq1/config/jdbc_url)
fi

if [ -d "/secrets/oebsp" ]; then
    export HM_OEBS_API_PROXY_DB_USR_P=$(cat /secrets/oebsp/credentials/username)
    export HM_OEBS_API_PROXY_DB_PW_P=$(cat /secrets/oebsp/credentials/password)
    export HM_OEBS_API_PROXY_DB_URL_P=$(cat /secrets/oebsp/config/jdbc_url)
fi

set -x
exec java -jar /app.jar
