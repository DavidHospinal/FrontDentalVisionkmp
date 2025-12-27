package com.dentalvision.ai.platform

import coil3.ImageLoader

/**
 * Platform-specific ImageLoader configuration
 *
 * Each platform (Android, iOS, Desktop, Web) can implement custom
 * HTTP headers, caching strategies, and network configurations.
 */
expect fun createImageLoader(context: Any? = null): ImageLoader
