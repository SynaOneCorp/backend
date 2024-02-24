package com.example.plugins

import org.jetbrains.exposed.sql.*

fun configureDatabases() = Database
    .connect(
        url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",////wv4.h.filess.io:5432/SynaOneBackend_manrubber",
        user = "root",
        driver = "org.h2.Driver",
        password = ""
    )
