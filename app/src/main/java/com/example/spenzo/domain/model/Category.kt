package com.example.spenzo.domain.model

enum class Category(
    val displayName: String,
    val colorHex: String,
    val iconName: String
) {
    FOOD("Food", "#FF9500", "fork_knife"),
    TRANSPORT("Transport", "#007AFF", "car"),
    SHOPPING("Shopping", "#FF2D55", "shopping_bag"),
    BILLS("Bills", "#34C759", "receipt"),
    ENTERTAINMENT("Entertainment", "#AF52DE", "film_strip"),
    HEALTH("Health", "#FF3B30", "heart"),
    EDUCATION("Education", "#5856D6", "graduation_cap"),
    OTHER("Other", "#8E8E93", "archive_box")
}
