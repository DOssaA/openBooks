package com.darioossa.openbooks

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform