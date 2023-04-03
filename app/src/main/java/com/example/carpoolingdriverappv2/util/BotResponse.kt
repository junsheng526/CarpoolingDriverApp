package com.example.carpoolingdriverappv2.util

import com.example.carpoolingdriverappv2.util.Constants.OPEN_GOOGLE
import com.example.carpoolingdriverappv2.util.Constants.OPEN_SEARCH
import java.sql.Date
import java.sql.Timestamp
import java.text.SimpleDateFormat

object BotResponse {

    fun basicResponses(_message: String): String {

        val random = (0..2).random()
        val message =_message.toLowerCase()

        return when {

            //Flips a coin
            message.contains("flip") && message.contains("coin") -> {
                val r = (0..1).random()
                val result = if (r == 0) "heads" else "tails"

                "I flipped a coin and it landed on $result"
            }

            //Math calculations
            message.contains("solve") -> {
                val equation: String? = message.substringAfterLast("solve")
                return try {
                    val answer = SolveMath.solveMath(equation ?: "0")
                    "$answer"

                } catch (e: Exception) {
                    "Sorry, I can't solve that."
                }
            }

            //Hello
            message.contains("hello") -> {
                when (random) {
                    0 -> "Hello there!"
                    1 -> "嗨，我在！"
                    2 -> "Bonjour!"
                    else -> "error" }
            }

            //How are you?
            message.contains("how are you") -> {
                when (random) {
                    0 -> "I'm doing fine, thanks!"
                    1 -> "I'm hungry..."
                    2 -> "Pretty good! How about you?"
                    else -> "error"
                }
            }

            //What time is it?
            message.contains("time") && message.contains("?")-> {
                val timeStamp = Timestamp(System.currentTimeMillis())
                val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm")
                val date = sdf.format(Date(timeStamp.time))

                date.toString()
            }

            //Open Google
            message.contains("open") && message.contains("google")-> {
                OPEN_GOOGLE
            }

            //Search on the internet
            message.contains("search")-> {
                OPEN_SEARCH
            }

            message.contains("carpool") -> {
                when (random) {
                    0 -> "Carpooling is the sharing of car journeys~"
                    1 -> "Carpooling is a more environmentally friendly and sustainable way to travel as sharing journeys reduces air pollution."
                    2 -> "Carpooling usually means to divide the travel expenses equally between all the occupants of the vehicle (driver or passenger)."
                    else -> "error"
                }
            }

            message.contains("only") && message.contains("home") -> {
                when (random) {
                    0 -> "Yes, you have to login first to use further features. "
                    1 -> "You may login to figure more."
                    2 -> "My dear, you must login before using another features. "
                    else -> "error"
                }
            }

            message.contains("create") && message.contains("trip") -> {
                when (random) {
                    0 -> "Firstly, open the drawer, then click 'carpool', lastly, click 'create trip'."
                    1 -> "Firstly, open the drawer, then click 'carpool', lastly, click 'create trip'."
                    2 -> "Firstly, open the drawer, then click 'carpool', lastly, click 'create trip'."
                    else -> "error"
                }
            }

            message.contains("love") && message.contains("?") -> {
                when (random) {
                    0 -> "Confirm is Jun Sheng"
                    1 -> "Maybe Ee Heng"
                    2 -> "Probably is Sheyn"
                    else -> "error"
                }
            }

            message.contains("EeHeng") && message.contains("age") -> {
                when (random) {
                    0 -> "23 years old"
                    1 -> "Born in 1999"
                    2 -> "90's"
                    else -> "error"
                }
            }

            message.contains("handsome") || message.contains("lengzai") -> {
                when (random) {
                    0 -> "Jun Sheng"
                    1 -> "Choo Jun Sheng"
                    2 -> "Mr. Jun Sheng"
                    else -> "error"
                }
            }



            //When the programme doesn't understand...
            else -> {
                when (random) {
                    0 -> "I don't understand..."
                    1 -> "Try asking me something different"
                    2 -> "Idk"
                    else -> "error"
                }
            }
        }
    }
}