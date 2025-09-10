package com.example.androidgamekt

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CalendarView
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.androidgamekt.ui.theme.AndroidGameKTTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class Player(
    val fullName: String,
    val gender: String,
    val course: String,
    val difficulty: Int,
    val birthDate: String,
    val zodiacSign: String
)

class MainActivity : ComponentActivity() {
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signup_activity)

        val etFullName = findViewById<EditText>(R.id.etFullName)
        val rgGender = findViewById<RadioGroup>(R.id.rgGender)
        val spCourse = findViewById<Spinner>(R.id.spCourse)
        val sbDifficulty = findViewById<SeekBar>(R.id.sbDifficulty)
        val cvBirthDate = findViewById<CalendarView>(R.id.cvBirthDate)
        val ivZodiac = findViewById<ImageView>(R.id.ivZodiac)
        val btnSubmit = findViewById<Button>(R.id.btnSubmit)
        val tvResult = findViewById<TextView>(R.id.tvResult)

        val courses = arrayOf("1 курс", "2 курс", "3 курс", "4 курс", "5 курс", "6 курс")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, courses)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spCourse.adapter = adapter

        var selectedDate = ""

        cvBirthDate.setOnDateChangeListener { _, year, month, day ->
            selectedDate = "$day/${month + 1}/$year"
        }

        btnSubmit.setOnClickListener {
            val fullName = etFullName.text.toString()
            val gender = when (rgGender.checkedRadioButtonId) {
                R.id.rbMale -> "Мужской"
                R.id.rbFemale -> "Женский"
                else -> "Не указан"
            }

            if (fullName.isEmpty()) {
                Toast.makeText(this, "Пожалуйста, введите ФИО", Toast.LENGTH_SHORT).show()
                etFullName.requestFocus()
                return@setOnClickListener
            }

            if (gender == "Не указан") {
                Toast.makeText(this, "Пожалуйста, выберите пол", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedDate.isEmpty()) {
                Toast.makeText(this, "Пожалуйста, выберите дату рождения", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!fullName.matches(Regex("^[А-Яа-яA-Za-z\\s-]+$")) || fullName.split("\\s+".toRegex()).size < 2) {
                Toast.makeText(this, "ФИО должно содержать минимум 2 слова и только буквы, пробелы или дефисы", Toast.LENGTH_LONG).show()
                etFullName.requestFocus()
                return@setOnClickListener
            }

            val course = spCourse.selectedItem.toString()
            val difficulty = sbDifficulty.progress
            val zodiacSign = getZodiacSign(selectedDate)

            val zodiacDrawable = when (zodiacSign) {
                "Овен" -> R.drawable.aries
                "Телец" -> R.drawable.taurus
                "Близнецы" -> R.drawable.gemini
                "Рак" -> R.drawable.cancer
                "Лев" -> R.drawable.leo
                "Дева" -> R.drawable.virgo
                "Весы" -> R.drawable.libra
                "Скорпион" -> R.drawable.scorpio
                "Стрелец" -> R.drawable.sagittarius
                "Козерог" -> R.drawable.capricorn
                "Водолей" -> R.drawable.aquarius
                "Рыбы" -> R.drawable.pisces
                else -> R.drawable.ic_launcher_background
            }
            ivZodiac.setImageResource(zodiacDrawable)

            val player = Player(fullName, gender, course, difficulty, selectedDate, zodiacSign)

            tvResult.text = """
                ФИО: ${player.fullName}
                Пол: ${player.gender}
                Курс: ${player.course}
                Уровень сложности: ${player.difficulty}
                Дата рождения: ${player.birthDate}
                Знак зодиака: ${player.zodiacSign}
            """.trimIndent()
        }
    }

    private fun getZodiacSign(date: String): String {
        if (date.isEmpty()) return "Не выбрана дата"

        try {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val birthDate = sdf.parse(date) ?: return "Ошибка даты"
            val calendar = Calendar.getInstance().apply { time = birthDate }
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val month = calendar.get(Calendar.MONTH) + 1

            return when {
                (month == 3 && day >= 21) || (month == 4 && day <= 19) -> "Овен"
                (month == 4 && day >= 20) || (month == 5 && day <= 20) -> "Телец"
                (month == 5 && day >= 21) || (month == 6 && day <= 20) -> "Близнецы"
                (month == 6 && day >= 21) || (month == 7 && day <= 22) -> "Рак"
                (month == 7 && day >= 23) || (month == 8 && day <= 22) -> "Лев"
                (month == 8 && day >= 23) || (month == 9 && day <= 22) -> "Дева"
                (month == 9 && day >= 23) || (month == 10 && day <= 22) -> "Весы"
                (month == 10 && day >= 23) || (month == 11 && day <= 21) -> "Скорпион"
                (month == 11 && day >= 22) || (month == 12 && day <= 21) -> "Стрелец"
                (month == 12 && day >= 22) || (month == 1 && day <= 19) -> "Козерог"
                (month == 1 && day >= 20) || (month == 2 && day <= 18) -> "Водолей"
                else -> "Рыбы"
            }
        } catch (e: Exception) {
            return "Ошибка обработки даты"
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    AndroidGameKTTheme {
        Greeting("Android")
    }
}