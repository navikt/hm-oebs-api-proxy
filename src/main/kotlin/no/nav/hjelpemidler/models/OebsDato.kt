package no.nav.hjelpemidler.models

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private val oebsDatoFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

fun String.tilLocalDate(): LocalDate = LocalDateTime.parse(this, oebsDatoFormatter).toLocalDate()
