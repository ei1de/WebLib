package com.helper.wbm.dec

fun String.dec(shift:Int = 1):String{
    val decryptedMessage = StringBuilder()
    for (char in this) {
        if (char.isLetter()) {
            val baseChar = if (char.isLowerCase()) 'a' else 'A'
            val shiftedChar = ((char.code - baseChar.code - shift + 26) % 26 + baseChar.code).toChar()
            decryptedMessage.append(shiftedChar)
        } else {
            decryptedMessage.append(char)
        }
    }
    return decryptedMessage.toString()
}