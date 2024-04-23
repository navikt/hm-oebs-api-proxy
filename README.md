# hm-oebs-api-proxy
Applikasjon som snakker med oracle databasen til OEBS.

## Teknologiar
Prosjektet bruker Kotlin og Gradle, i tillegg til ktor.


# Førstegangs kjøring

Første gang man kjører opp appen lokalt, må schema og klient 
til GraphQL apiet mot grunndata-api, lastes ned og genereres. 

Kjør gradle taskene graphqlIntrospectSchema og graphqlGenerateClient

# Greit å vite

## Koble mot T2
- Bruk samme creds som Q1 `OEBS_DB=oebsq1`.
- JDBC_URL ligger i `OEBS_DB_JDBC_URL_T2` i k8s secret. Husk å endre til denne i dataSource.

## Om XXRTV_DIGIHOT_HJM_UTLAN_FNR_V
__artikkelStatus__ kan ha verdiene `I utlån` og `Til reparasjon`. Det er ca 23 status'er tilgjengelig (teknisk), men view'et plukker kun ut artikler med status'ene 'Til reparasjon' og 'I utlån', slik det ble avtalt når view'et ble opprettet.
