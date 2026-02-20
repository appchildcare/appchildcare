package com.ys.phdmama.model

data class PoopRecord(
    val timestamp: Long = System.currentTimeMillis(),
    val time: String = "",
    val color: String = "",
    val texture: String = "",
    val size: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class DayPoopEntry(
    val dayName: String,
    val poops: List<PoopRecord>
)

data class WeekDay(
    val name: String,
    val isSelected: Boolean = false,
    val poopCount: Int = 0
)

enum class PoopColor(val displayName: String, val value: String) {
    MUY_OSCURO("Muy oscuro", "muy_oscuro"),
    VERDE_OSCURO("Verde oscuro", "verde_oscuro"),
    MARRON("Marrón", "marron"),
    AMARILLO("Amarillo", "amarillo"),
    OTROS("Otros", "otros")
}

enum class PoopTexture(val displayName: String, val value: String) {
    LIQUIDA("Líquida", "liquida"),
    PASTOSA("Pastosa", "pastosa"),
    DURA("Dura", "dura"),
    CON_MOCOS("Con mocos", "con_mocos"),
    CON_SANGRE("Con sangre", "con_sangre"),
    AGRIETADA("Agrietada", "agrietada"),
    SEPARADA("Separada", "separada")
}

enum class PoopSize(val displayName: String, val value: String) {
    MONEDA("Moneda", "moneda"),
    CUCHARA_SOPERA("Cuchara sopera", "cuchara_sopera"),
    MAS_GRANDE("Más grande", "mas_grande")
}
