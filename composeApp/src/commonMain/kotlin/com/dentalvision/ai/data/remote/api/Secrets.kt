package com.dentalvision.ai.data.remote.api

/**
 * API Secrets Configuration - Contest Demo Version
 *
 * IMPORTANT NOTICE FOR CONTEST JUDGES:
 * This file contains a pre-configured demo API key specifically for
 * Kotlin Multiplatform Contest evaluation purposes.
 *
 * ⚠️ API KEY SECURITY DISCLAIMER:
 * Due to GitHub's automated security scanning, this API key may be automatically
 * revoked by Google before your evaluation. This is a known limitation of public
 * repositories with embedded API keys.
 *
 * IF CLINICAL INSIGHTS FEATURE SHOWS 403 ERRORS:
 * The key was auto-revoked by Google's security system. To restore functionality:
 * 1. Obtain a free API key at: https://aistudio.google.com/app/apikey (takes 2 minutes)
 * 2. Replace the GEMINI_API_KEY value below with your new key
 * 3. Rebuild the project: ./gradlew clean
 *
 * All other features (Patient Management, Dental Analysis with YOLOv12, Reports)
 * work without any configuration and are not affected by this limitation.
 *
 * LAST KEY UPDATE: January 11, 2026, 23:30 UTC
 * STATUS AT TIME OF COMMIT: Active and verified working
 *
 * For production use, replace with your own API key from:
 * https://aistudio.google.com/app/apikey
 *
 * SECURITY NOTE:
 * In production environments, this file should NEVER be committed to
 * version control and should be listed in .gitignore.
 */
object Secrets {
    /**
     * Google Gemini API Key - Demo Configuration
     * Valid for contest evaluation period only
     *
     * Current key: Active (as of January 11, 2026, 23:30 UTC)
     * Previous key ending in ...N9s was auto-revoked by Google on January 11, 2026
     */
    const val GEMINI_API_KEY = "AIzaSyBOnGud8rqIOCqv2MkXQwD829-hDl1qgsk"
}
