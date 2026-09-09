package com.loantracker.app.data

import androidx.room.TypeConverter
import java.time.LocalDate

class Converters {
    @TypeConverter
    fun fromEpochDay(value: Long?): LocalDate? = value?.let { LocalDate.ofEpochDay(it) }

    @TypeConverter
    fun localDateToEpochDay(date: LocalDate?): Long? = date?.toEpochDay()

    @TypeConverter
    fun fromTipoJuros(value: String?): TipoJuros? = value?.let { TipoJuros.valueOf(it) }

    @TypeConverter
    fun tipoJurosToString(value: TipoJuros?): String? = value?.name

    @TypeConverter
    fun fromFrequencia(value: String?): FrequenciaParcela? = value?.let { FrequenciaParcela.valueOf(it) }

    @TypeConverter
    fun frequenciaToString(value: FrequenciaParcela?): String? = value?.name
}
