package com.example.upionemoretime.voice

enum class VoiceState {
    IDLE,
    WAKING,     // Listening for "Hey Assistant"
    PROMPTING,  // Speaking "How can I help you?" or "I didn't catch that"
    LISTENING,  // Active command listening
    PROCESSING, // Parsing the voice input
    RESPONDING, // Speaking the result of a command
    CLOSING     // Speaking a final message before returning to WAKING
}
