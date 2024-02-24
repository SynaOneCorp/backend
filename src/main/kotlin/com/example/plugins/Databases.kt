package com.example.plugins

import org.jetbrains.exposed.sql.*

fun configureDatabases() = Database
    .connect(
//        url = "jdbc:h2:file:./build/db;DB_CLOSE_DELAY=-1",
        url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",
        user = "root",
        driver = "org.h2.Driver",
        password = ""
    )

