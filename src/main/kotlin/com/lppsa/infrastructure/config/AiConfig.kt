package com.lppsa.infrastructure.config

import com.lppsa.domain.model.RequestType
import org.springframework.ai.chat.client.ChatClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource

@Configuration
class AiConfig {

    @Bean
    fun chatClient(builder: ChatClient.Builder): ChatClient = builder.build()

    fun loadPolicy(requestType: RequestType): String {
        val regulamin = ClassPathResource("policy/regulamin.md").inputStream.bufferedReader().readText()
        val specific = when (requestType) {
            RequestType.REKLAMACJA -> ClassPathResource("policy/reklamacje.md").inputStream.bufferedReader().readText()
            RequestType.ZWROT -> ClassPathResource("policy/zwrot-30-dni.md").inputStream.bufferedReader().readText()
        }
        return "$regulamin\n\n---\n\n$specific"
    }
}
