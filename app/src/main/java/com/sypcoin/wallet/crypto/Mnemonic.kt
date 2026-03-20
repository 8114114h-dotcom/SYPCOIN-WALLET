package com.sypcoin.wallet.crypto

import java.security.SecureRandom

/**
 * BIP-39-inspired mnemonic phrase generation for Sypcoin wallets.
 *
 * Uses 12 words from a minimal wordlist.
 * In production replace WORDLIST with the full 2048-word BIP-39 English list.
 */
object Mnemonic {

    private val WORDLIST = arrayOf(
        "abandon","ability","able","about","above","absent","absorb","abstract",
        "absurd","abuse","access","accident","account","accuse","achieve","acid",
        "acoustic","acquire","across","act","action","actor","actress","actual",
        "adapt","add","addict","address","adjust","admit","adult","advance",
        "advice","aerobic","afford","afraid","again","age","agent","agree",
        "ahead","aim","air","airport","aisle","alarm","album","alcohol",
        "alert","alien","all","alley","allow","almost","alone","alpha",
        "already","also","alter","always","amateur","amazing","among","amount",
        "amused","analyst","anchor","ancient","anger","angle","angry","animal",
        "ankle","announce","annual","another","answer","antenna","antique","anxiety",
        "any","apart","apology","appear","apple","approve","april","arch",
        "arctic","area","arena","argue","arm","armed","armor","army",
        "around","arrange","arrest","arrive","arrow","art","artefact","artist",
        "artwork","ask","aspect","assault","asset","assist","assume","asthma",
        "athlete","atom","attack","attend","attitude","attract","auction","audit",
        "august","aunt","author","auto","autumn","average","avocado","avoid",
        "awake","aware","away","awesome","awful","awkward","axis","baby"
    )

    const val WORD_COUNT = 12

    /** Generate a fresh 12-word mnemonic using SecureRandom. */
    fun generate(): String {
        val random = SecureRandom()
        val bytes  = ByteArray(16)
        random.nextBytes(bytes)

        return (0 until WORD_COUNT).map { i ->
            val idx = (bytes[i % bytes.size].toInt() and 0xFF) xor
                      ((bytes[(i + 1) % bytes.size].toInt() and 0xFF) shr 4)
            WORDLIST[idx % WORDLIST.size]
        }.joinToString(" ")
    }

    /** Validate that every word in the phrase is in the wordlist. */
    fun validate(phrase: String): Boolean {
        val words = phrase.trim().split("\\s+".toRegex())
        if (words.size != WORD_COUNT) return false
        return words.all { it in WORDLIST }
    }

    /** Derive a 32-byte seed from a mnemonic phrase. */
    fun toSeed(phrase: String): ByteArray {
        val pre = "SYPCOIN_MNEMONIC_V1".toByteArray() + phrase.toByteArray()
        return sha256(pre)
    }

    private fun sha256(input: ByteArray): ByteArray {
        return java.security.MessageDigest.getInstance("SHA-256").digest(input)
    }
}
