package org.big_ear_pig.small_pig.module.table

enum class DataType(val displayName: String, val code: String) {
    TEXT("文本", "TEXT"),
    NUMBER("数字", "NUMBER"),
    DAY("日期", "DAY"),
    TIME("时间", "TIME");

    companion object {
        fun fromCode(code: String): DataType? {
            return values().find { it.code.equals(code, ignoreCase = true) }
        }

        fun fromDisplayName(displayName: String): DataType? {
            return values().find { it.displayName == displayName }
        }
    }
}