package com.mtaa.app

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform