package io.github.halilozel1903.tvsearch

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import io.github.halilozel1903.tvsearch.core.VoiceResults

/**
 * What to ask the speech recognizer for.
 *
 * @param prompt shown by the recognizer while it listens, for example "Say a title".
 * @param language an IETF language tag such as `"en-US"`; `null` uses the device language.
 * @param maxResults how many guesses the recognizer may return; the most confident one is used.
 */
@Immutable
public data class VoiceSearchRequest(
    public val prompt: String? = null,
    public val language: String? = null,
    public val maxResults: Int = 3,
)

/**
 * An [ActivityResultContract] around [RecognizerIntent.ACTION_RECOGNIZE_SPEECH] that returns the
 * recognized text (picked by [VoiceResults.best]) or `null` when the user cancelled or nothing was
 * understood.
 */
public class RecognizeSpeech : ActivityResultContract<VoiceSearchRequest, String?>() {
    override fun createIntent(context: Context, input: VoiceSearchRequest): Intent = VoiceSearch.intent(input)

    override fun parseResult(resultCode: Int, intent: Intent?): String? {
        if (resultCode != Activity.RESULT_OK || intent == null) return null
        return VoiceResults.best(
            intent.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS),
            intent.getFloatArrayExtra(RecognizerIntent.EXTRA_CONFIDENCE_SCORES),
        )
    }
}

/** Helpers for the system speech recognizer. */
public object VoiceSearch {

    /** The recognizer intent for [request]: free form language model, optional prompt and language. */
    public fun intent(request: VoiceSearchRequest = VoiceSearchRequest()): Intent =
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, request.maxResults.coerceAtLeast(1))
            request.prompt?.let { putExtra(RecognizerIntent.EXTRA_PROMPT, it) }
            request.language?.let { putExtra(RecognizerIntent.EXTRA_LANGUAGE, it) }
        }

    /**
     * `true` when the device has an app that answers [RecognizerIntent.ACTION_RECOGNIZE_SPEECH].
     * The library's manifest declares the `<queries>` entry this needs on Android 11 and newer.
     */
    public fun isAvailable(context: Context): Boolean {
        if (SpeechRecognizer.isRecognitionAvailable(context)) return true
        @Suppress("DEPRECATION")
        val activities = context.packageManager.queryIntentActivities(intent(), PackageManager.MATCH_DEFAULT_ONLY)
        return activities.isNotEmpty()
    }
}

/**
 * Starts voice search. Get one from [rememberVoiceSearchLauncher].
 *
 * @property isAvailable whether the device has a speech recognizer. Hide or disable the mic when it
 * is `false`; [launch] then does nothing and returns `false`.
 */
@Stable
public class VoiceSearchLauncher internal constructor(
    public val isAvailable: Boolean,
    private val start: () -> Boolean,
) {
    /** Opens the recognizer. Returns `false` when no recognizer could be started. */
    public fun launch(): Boolean = start()
}

/**
 * Remembers a [VoiceSearchLauncher] that opens the system speech recognizer and passes the
 * recognized text to [onResult]. Cancelled or empty recognitions do not call [onResult].
 */
@Composable
public fun rememberVoiceSearchLauncher(
    request: VoiceSearchRequest = VoiceSearchRequest(),
    onResult: (String) -> Unit,
): VoiceSearchLauncher {
    val context = LocalContext.current
    val currentOnResult by rememberUpdatedState(onResult)
    val contract = remember { RecognizeSpeech() }
    val launcher = rememberLauncherForActivityResult(contract) { text ->
        if (!text.isNullOrBlank()) currentOnResult(text)
    }
    val available = remember(context) { VoiceSearch.isAvailable(context) }
    return remember(launcher, available, request) {
        VoiceSearchLauncher(available) {
            if (!available) {
                false
            } else {
                try {
                    launcher.launch(request)
                    true
                } catch (e: ActivityNotFoundException) {
                    false
                }
            }
        }
    }
}

/**
 * Remembers a [VoiceSearchLauncher] that searches what the user said in [state]: the text replaces
 * the query, is searched right away and is recorded as a recent search.
 */
@Composable
public fun rememberVoiceSearchLauncher(
    state: TvSearchState,
    request: VoiceSearchRequest = VoiceSearchRequest(),
): VoiceSearchLauncher = rememberVoiceSearchLauncher(request) { text ->
    state.submit(text)
    state.requestFocus(SearchFocusTarget.Result(0))
}
