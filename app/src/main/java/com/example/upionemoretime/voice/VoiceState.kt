package com.example.upionemoretime.voice

enum class VoiceState {
    IDLE,
    WAKING,         // Listening for "Hey Assistant"
    PROMPTING,      // Speaking "How can I help you?" or "I didn't catch that"
    LISTENING,      // Active command listening
    PROCESSING,     // Parsing the voice input
    RESPONDING,     // Speaking the result of a command
    CLOSING,        // Speaking a final message before returning to WAKING
    
    // Biometric States (Sequential)
    ENROLLING,           // Phase 1: Text verification
    ENROLLING_VOICE,     // Phase 2: Voice print capture
    AUTHENTICATING,      // Phase 1: Text verification
    AUTHENTICATING_VOICE, // Phase 2: Voice print capture
    UNAUTHORIZED
}
