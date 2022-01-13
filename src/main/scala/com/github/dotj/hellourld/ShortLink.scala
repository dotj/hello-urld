package com.github.dotj.hellourld

import java.net.URL
import java.time.Instant
import java.util.UUID

case class ShortLink(
    id: UUID = UUID.randomUUID(),
    token: String,
    redirectTo: URL,
    createdDtm: Instant = Instant.now(),
    expirationDtm: Option[Instant] = None
)
