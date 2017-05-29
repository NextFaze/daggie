package com.nextfaze.daggie.logback

import android.util.Log
import ch.qos.logback.classic.Level
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

private val NEW_LINE = System.getProperty("line.separator")
private val NON_BREAKING_SPACE = '\u00A0'

/**
 * Max logcat len is defined as:
 *
 * ```
 *      #define LOGGER_ENTRY_MAX_LEN      4076
 *          (see /system/core/include/cutils/logger.h:57)
 * ```
 *
 * Thus 4000 for "just-in-case".
 */
private val MAX_OUTPUT_LEN = 4000

private val SPLIT_LEN = 3000
private val NEW_LINE_BEFORE_SPLIT = 200
private val NEW_LINE_AFTER_SPLIT = 1000

private val SPLIT_MESSAGE = "<continues from previous>" + NEW_LINE

private val NEW_LINE_BS = NEW_LINE + " "
private val NEW_LINE_NBS = NEW_LINE + NON_BREAKING_SPACE

/** Ported from `com.nextfaze:slf4j-log4j-android` `com.nextfaze.logging.LogCatAppender` class.  */
@Singleton
internal class LogCatLogger @Inject constructor() {

    fun log(tag: String, message: String, levelInt: Int) {
        // add no-break-space to allow proper formatting
        var msg = message.replace(NEW_LINE_BS, NEW_LINE_NBS)

        // output if not too long
        if (msg.length <= MAX_OUTPUT_LEN) {
            logMsg(tag, msg, levelInt)
            return
        }

        // split long messages
        val msgs = ArrayList<String>()
        while (msg.length > MAX_OUTPUT_LEN) {
            val newLineIdx = msg.indexOf(NEW_LINE, SPLIT_LEN - NEW_LINE_BEFORE_SPLIT)
            if (newLineIdx == -1 || newLineIdx >= SPLIT_LEN + NEW_LINE_AFTER_SPLIT) {
                msgs.add(msg.substring(0, SPLIT_LEN))
                msg = msg.substring(SPLIT_LEN + 1)
            } else {
                msgs.add(msg.substring(0, newLineIdx))
                msg = msg.substring(newLineIdx + NEW_LINE.length)
            }
        }
        msgs.add(msg) // last segment

        // process message segments
        msgs.indices.forEach { i ->
            val m = msgs[i]

            // add segment info
            val builder = StringBuilder(m.length + 50)
            builder.append("[").append(i + 1).append("/").append(msgs.size).append("] ")
            if (i > 0) {
                builder.append(SPLIT_MESSAGE)
            }
            builder.append(m)

            logMsg(tag, builder.toString(), levelInt)
        }
    }

    private fun logMsg(tag: String, msg: String, level: Int) {
        when (level) {
            Level.ALL_INT -> Log.v(tag, msg)
            Level.TRACE_INT -> Log.v(tag, msg)
            Level.DEBUG_INT -> Log.d(tag, msg)
            Level.INFO_INT -> Log.i(tag, msg)
            Level.WARN_INT -> Log.w(tag, msg)
            Level.ERROR_INT -> Log.e(tag, msg)
        }
    }
}
