CREATE SCHEMA IF NOT EXISTS apps;

-- Bruker tabell for å simulere view i OEBS
CREATE TABLE IF NOT EXISTS apps.xxrtv_digihot_hjm_utlan_fnr_v
(
    forekomst_nummer           VARCHAR2(30),
    ordre_nummer               NUMBER,
    antall                     NUMBER,
    kategori3_nummer           VARCHAR2(163),
    kategori3_beskrivelse      VARCHAR2(240),
    artikkelstatus             VARCHAR2(50),
    utlåns_type                VARCHAR2(240),
    utlåns_dato                DATE,
    opprettelsesdato           DATE,
    innleveringsdato           DATE,
    oppdatert_innleveringsdato DATE,
    serie_nummer               VARCHAR2(30),
    artikkel_beskrivelse       VARCHAR2(240),
    artikkelnummer             VARCHAR2(40),
    enhet                      VARCHAR2(3),
    bruker_nummer              VARCHAR2(30),
    fnr                        VARCHAR2(30),
    egen_ansatt                VARCHAR2(150),
    navn                       VARCHAR2(360),
    installasjons_addresse     VARCHAR2(722),
    installasjons_kommune      VARCHAR2(60),
    installasjons_postnummer   VARCHAR2(60),
    installasjons_by           VARCHAR2(60),
    bosteds_addresse           VARCHAR2(722),
    bosteds_kommune            VARCHAR2(60),
    bosteds_postnummer         VARCHAR2(60),
    bosteds_by                 VARCHAR2(60)
);

INSERT INTO apps.xxrtv_digihot_hjm_utlan_fnr_v(forekomst_nummer, ordre_nummer, antall, kategori3_nummer,
                                               kategori3_beskrivelse, artikkelstatus, utlåns_type, utlåns_dato,
                                               opprettelsesdato, innleveringsdato, oppdatert_innleveringsdato,
                                               serie_nummer, artikkel_beskrivelse, artikkelnummer, enhet, bruker_nummer,
                                               fnr, egen_ansatt, navn, installasjons_addresse, installasjons_kommune,
                                               installasjons_postnummer, installasjons_by, bosteds_addresse,
                                               bosteds_kommune, bosteds_postnummer, bosteds_by)
VALUES ('', 1, 1, '', '', '', '', '2020-02-01', '2020-01-01', '2020-10-01', '2020-11-01', '', '', '', '', '',
        '19127428657', '', '', '', '', '0000', '', '', '', '0000', '');

-- Bruker tabell for å simulere view i OEBS
CREATE TABLE IF NOT EXISTS apps.xxrtv_digihot_oebs_adr_fnr_v
(
    bruker_nummer        VARCHAR2(30),
    fnr                  VARCHAR2(30),
    egen_ansatt          VARCHAR2(150),
    navn                 VARCHAR2(360),
    bosteds_addresse     VARCHAR2(722),
    bosteds_kommune      VARCHAR2(60),
    bosteds_postnummer   VARCHAR2(60),
    bosteds_by           VARCHAR2(60),
    bydel                VARCHAR2(60),
    leverings_addresse   VARCHAR2(722),
    leverings_kommune    VARCHAR2(60),
    leverings_postnummer VARCHAR2(60),
    leverings_by         VARCHAR2(60),
    primaer_adr          VARCHAR2(1),
    status_brukernr      VARCHAR2(1),
    status_fnr           VARCHAR2(1),
    status_adr_p_sted    VARCHAR2(1),
    status_adr_c_sted    VARCHAR2(1),
    status_adr_bru       VARCHAR2(1)
);

INSERT INTO apps.xxrtv_digihot_oebs_adr_fnr_v (bruker_nummer, fnr, egen_ansatt, navn, bosteds_addresse, bosteds_kommune,
                                               bosteds_postnummer, bosteds_by, bydel, leverings_addresse,
                                               leverings_kommune, leverings_postnummer, leverings_by, primaer_adr,
                                               status_brukernr, status_fnr, status_adr_p_sted, status_adr_c_sted,
                                               status_adr_bru)
VALUES ('1', '19127428657', '', '', 'test', 'test', '0000', 'test', 'test', 'test', 'test', '0000', 'test', '0', 'A',
        'A', '', '', ''),
       ('1', '01987654321', '', '', '.', '', '', '', '', '', '', '', '', '0', 'A', 'A', '', '', ''),
       ('1', '01011121314', '', '', 'test', 'test', '0000', 'test', 'test', 'test', 'test', '0000', 'test', '0', 'I',
        'I', '', '', '');

-- Bruker tabell for å simulere view i OEBS
CREATE TABLE IF NOT EXISTS apps.xxrtv_digihot_oebs_art_beskr_v
(
    artikkel               VARCHAR2(40),
    artikkel_beskrivelse_b VARCHAR2(240),
    artikkel_beskrivelse   VARCHAR2(240),
    brukerartikkeltype     VARCHAR2(80),
    language               VARCHAR2(4),
    listepris_enhet        NUMBER,
    org_id                 NUMBER
);

INSERT INTO apps.xxrtv_digihot_oebs_art_beskr_v(artikkel, artikkel_beskrivelse_b, artikkel_beskrivelse,
                                                brukerartikkeltype, language, listepris_enhet, org_id)
VALUES ('1', 'test', 'test', 'test', '', 0, 0);

-- Bruker tabell for å simulere view i OEBS
CREATE TABLE IF NOT EXISTS apps.xxrtv_digihot_oebs_brukerp_v
(
    fnr             VARCHAR2(30),
    kontrakt_nummer VARCHAR2(150),
    sjekk_navn      VARCHAR2(150),
    start_date      DATE,
    end_date        DATE
);

INSERT INTO apps.xxrtv_digihot_oebs_brukerp_v(fnr, kontrakt_nummer, sjekk_navn, start_date, end_date)
VALUES ('19127428657', 'test', 'test', '2020-01-01', NULL);

-- Bruker tabell for å simulere view i OEBS
CREATE TABLE IF NOT EXISTS apps.xxrtv_digihot_utvid_art_v
(
    organisasjons_id     NUMBER(15),
    organisasjons_navn   VARCHAR2(240),
    artikkelnummer       VARCHAR2(40),
    artikkelid           NUMBER,
    artikkel_beskrivelse VARCHAR2(240),
    kategori_nummer      VARCHAR2(4000),
    kategori_beskrivelse VARCHAR2(4000),
    fysisk               NUMBER,
    tilgjengeligatt      NUMBER,
    tilgjengeligroo      NUMBER,
    tilgjengelig         NUMBER,
    behovsmeldt          NUMBER,
    reservert            NUMBER,
    restordre            NUMBER,
    bestillinger         NUMBER,
    anmodning            NUMBER,
    intanmodning         NUMBER,
    forsyning            NUMBER,
    sortiment            VARCHAR2(4000),
    lagervare            VARCHAR2(4000),
    minmax               VARCHAR2(4000)
);

INSERT INTO apps.xxrtv_digihot_utvid_art_v(organisasjons_id, organisasjons_navn, artikkelnummer, artikkelid,
                                           artikkel_beskrivelse, kategori_nummer, kategori_beskrivelse, fysisk,
                                           tilgjengeligatt, tilgjengeligroo, tilgjengelig, behovsmeldt, reservert,
                                           restordre, bestillinger, anmodning, intanmodning, forsyning, sortiment,
                                           lagervare, minmax)
VALUES (123456789, 'test', '123456', 1, '', '', '', 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, '', '', '');

CREATE SEQUENCE IF NOT EXISTS apps.xxrtv_cs_digihot_sf_opprett_s;

-- Bruker tabell for å simulere view i OEBS
CREATE TABLE IF NOT EXISTS apps.xxrtv_cs_digihot_sf_opprett
(
    id                   NUMBER DEFAULT apps.xxrtv_cs_digihot_sf_opprett_s.nextval,
    fnr                  VARCHAR2(30),
    navn                 VARCHAR2(360),
    stonadsklass         VARCHAR2(255),
    sakstype             VARCHAR2(255),
    resultat             VARCHAR2(255),
    sfdato               DATE,
    referansenummer      VARCHAR2(255),
    kilde                VARCHAR2(255),
    processed            VARCHAR2(255),
    last_update_date     DATE,
    last_updated_by      VARCHAR2(255),
    creation_date        DATE,
    created_by           VARCHAR2(255),
    job_id               VARCHAR2(255),
    saksblokk            VARCHAR2(255),
    beskrivelse          VARCHAR2(255),
    json_artikkelinfo_in TEXT,

    PRIMARY KEY (id)
);
