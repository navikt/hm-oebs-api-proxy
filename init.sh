if [ -d "/secrets/oebst1" ]; then
    export HM_OEBS_API_PROXY_DB_USR_T1=$(cat /secrets/oebst1/credentials/username)
    export HM_OEBS_API_PROXY_DB_PW_T1=$(cat /secrets/oebst1/credentials/password)
fi

if [ -d "/secrets/oebsq1" ]; then
    export HM_OEBS_API_PROXY_DB_USR_Q1=$(cat /secrets/oebsq1/credentials/username)
    export HM_OEBS_API_PROXY_DB_PW_Q1=$(cat /secrets/oebsq1/credentials/password)
fi

if [ -d "/secrets/oebsp" ]; then
    export HM_OEBS_API_PROXY_DB_USR_P=$(cat /secrets/oebsp/credentials/username)
    export HM_OEBS_API_PROXY_DB_PW_P=$(cat /secrets/oebsp/credentials/password)
fi
