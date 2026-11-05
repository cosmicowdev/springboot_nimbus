package com.nimbus.iam

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class NimbusIamApplication

fun main(args: Array<String>) {
    runApplication<NimbusIamApplication>(*args)
}
