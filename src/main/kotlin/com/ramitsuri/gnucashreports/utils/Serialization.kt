package com.ramitsuri.gnucashreports.utils

import com.ramitsuri.gnucashreports.model.report.MonthYear
import java.math.BigDecimal
import kotlinx.datetime.Month
import kotlinx.datetime.number
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

class BigDecimalSerializer : KSerializer<BigDecimal> {
    override val descriptor = PrimitiveSerialDescriptor("BigDecimal", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: BigDecimal) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): BigDecimal {
        return BigDecimal(decoder.decodeString())
    }
}

class MonthYearSerializer : KSerializer<MonthYear> {
    override val descriptor = PrimitiveSerialDescriptor("MonthYear", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: MonthYear) {
        encoder.encodeString("${value.year}-${value.month.number.toString().padStart(2, '0')}")
    }

    override fun deserialize(decoder: Decoder): MonthYear {
        return decoder.decodeString().let { string ->
            string.split("-").let {
                MonthYear(year = it[0].toInt(), month = Month(it[1].toInt()))
            }
        }
    }
}
