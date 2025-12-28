package com.dentalvision.ai.platform

import coil3.ImageLoader
import coil3.PlatformContext
import coil3.network.ktor3.KtorNetworkFetcherFactory
import io.ktor.client.HttpClient
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.headers

/**
 * Configures Coil ImageLoader with custom HTTP headers for HuggingFace compatibility (Desktop/JVM)
 *
 * HuggingFace Gradio API requires browser-like headers to serve images properly.
 * This configuration adds User-Agent, Accept, and Referer headers to all image requests.
 */
actual fun createImageLoader(context: Any?): ImageLoader {
    val platformContext = context as PlatformContext

    // Create Ktor HttpClient with custom headers for HuggingFace
    val httpClient = HttpClient {
        defaultRequest {
            headers {
                append("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                append("Accept", "image/webp,image/apng,image/*,*/*;q=0.8")
                append("Referer", "https://davidhosp-dental-vision-yolo12.hf.space/")
            }
        }
    }

    return ImageLoader.Builder(platformContext)
        .components {
            // Use Ktor with custom headers for network fetching
            add(KtorNetworkFetcherFactory(httpClient = httpClient))
        }
        .build()
}
