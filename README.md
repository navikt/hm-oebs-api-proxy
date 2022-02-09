# hm-oebs-api-proxy
Applikasjon som snakker med oracle databasen til OEBS.

## Teknologiar
Prosjektet bruker Kotlin og Gradle, i tillegg til ktor.


# Førstegangs kjøring

Første gang man kjører opp appen lokalt, må schema og klient 
til GraphQL apiet mot grunndata-api, lastes ned og genereres. 

Kjør gradle taskene graphqlIntrospectSchema og graphqlGenerateClient

