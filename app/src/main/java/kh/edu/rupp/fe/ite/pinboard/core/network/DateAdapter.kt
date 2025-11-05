package kh.edu.rupp.fe.ite.pinboard.core.network

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DateAdapter : JsonDeserializer<Date>, JsonSerializer<Date> {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): Date {
        return try {
            dateFormat.parse(json.asString) ?: Date()
        } catch (e: Exception) {
            Date()
        }
    }

    override fun serialize(
        src: Date,
        typeOfSrc: Type,
        context: JsonSerializationContext
    ): JsonElement {
        return JsonPrimitive(dateFormat.format(src))
    }
}