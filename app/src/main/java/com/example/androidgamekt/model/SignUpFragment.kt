package com.example.androidgamekt.model

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.androidgamekt.R
import com.example.androidgamekt.data.GameRepository
import com.example.androidgamekt.data.PlayerEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class SignUpFragment : Fragment() {
    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.signup_activity, container, false)
        val repository = GameRepository(requireContext())

        val etFullName = view.findViewById<EditText>(R.id.etFullName)
        val rgGender = view.findViewById<RadioGroup>(R.id.rgGender)
        val spCourse = view.findViewById<Spinner>(R.id.spCourse)
        val sbDifficulty = view.findViewById<SeekBar>(R.id.sbDifficulty)
        val cvBirthDate = view.findViewById<CalendarView>(R.id.cvBirthDate)
        val ivZodiac = view.findViewById<ImageView>(R.id.ivZodiac)
        val btnSubmit = view.findViewById<Button>(R.id.btnSubmit)
        val tvResult = view.findViewById<TextView>(R.id.tvResult)

        val courses = arrayOf("1 курс", "2 курс", "3 курс", "4 курс", "5 курс", "6 курс")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, courses)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spCourse.adapter = adapter

        val difficultyNames = mapOf(
            0 to "Лёгкий",
            1 to "Средний",
            2 to "Сложный",
            3 to "Дезинсектор"
        )

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
                Toast.makeText(requireContext(), "Пожалуйста, введите ФИО", Toast.LENGTH_SHORT).show()
                etFullName.requestFocus()
                return@setOnClickListener
            }

            if (gender == "Не указан") {
                Toast.makeText(requireContext(), "Пожалуйста, выберите пол", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedDate.isEmpty()) {
                Toast.makeText(requireContext(), "Пожалуйста, выберите дату рождения", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!fullName.matches(Regex("^[А-Яа-яA-Za-z\\s-]+$")) || fullName.split("\\s+".toRegex()).size < 2) {
                Toast.makeText(requireContext(), "ФИО должно содержать минимум 2 слова и только буквы, пробелы или дефисы", Toast.LENGTH_LONG).show()
                etFullName.requestFocus()
                return@setOnClickListener
            }

            val course = spCourse.selectedItem.toString()
            val difficulty = sbDifficulty.progress
            GameSettings.instance.difficulty = difficulty // Сохранение выбранной сложности
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
            }.let { resId ->
                if (resources.getIdentifier(resources.getResourceEntryName(resId), "drawable", requireContext().packageName) == 0)
                    R.drawable.ic_launcher_background else resId
            }
            ivZodiac.setImageResource(zodiacDrawable)

            val player = PlayerEntity(
                fullName = fullName,
                gender = gender,
                course = course,
                difficulty = difficulty,
                birthDate = selectedDate,
                zodiacSign = zodiacSign
            )

            CoroutineScope(Dispatchers.Main).launch {
                withContext(Dispatchers.IO) { repository.insertPlayer(player) }
                tvResult.text = """
                    ФИО: ${player.fullName}
                    Пол: ${player.gender}
                    Курс: ${player.course}
                    Уровень сложности: ${difficultyNames[difficulty]}
                    Дата рождения: ${player.birthDate}
                    Знак зодиака: ${player.zodiacSign}
                """.trimIndent()
            }
        }

        return view
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