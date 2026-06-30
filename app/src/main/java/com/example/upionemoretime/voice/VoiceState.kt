package com.example.upionemoretime.voice

enum class VoiceState {
    IDLE,
    WAKING,         // Listening for "Hey Assistant"
    PROMPTING,      // Speaking "How can I help you?" or "I didn't catch that"
    PROMPTING_SILENT, // Just finished a speak() and needs to listen without a full help prompt
    LISTENING,      // Active command listening
    PROCESSING,     // Parsing the voice input
    RESPONDING,     // Speaking the result of a command
    CLOSING,        // Speaking a final message before returning to WAKING
    
    // Biometric States (Sequential)
    ENROLLING,           // Phase 1: Text verification
    ENROLLING_VOICE,     // Phase 2: Voice print capture
    AUTHENTICATING,      // Phase 1: Text verification
    AUTHENTICATING_VOICE, // Phase 2: Voice print capture
    AUTHENTICATING_FINGERPRINT, // Phase 3: Fingerprint authentication
    UNAUTHORIZED,

    // Login Flow States
    LOGIN_MOBILE,
    LOGIN_PASSWORD,
    LOGIN_CONFIRM,
    
    // Signup Flow States
    ONBOARDING_CHECK,
    SIGNUP_NAME,
    SIGNUP_NAME_CONFIRM,
    SIGNUP_MOBILE,
    SIGNUP_EMAIL,
    SIGNUP_PASSWORD,
    SIGNUP_CONFIRM_PASSWORD,
    SIGNUP_CONFIRM_FINAL,

    // Link Bank Flow
    LINK_BANK_SELECTION,
    LINK_BANK_CONFIRMATION,

    // OTP Flow States
    OTP_INPUT,
    OTP_CONFIRM
}
