package com.sertax.api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SerTaxApplication

fun main(args: Array<String>) {
	runApplication<SerTaxApplication>(*args)
}
