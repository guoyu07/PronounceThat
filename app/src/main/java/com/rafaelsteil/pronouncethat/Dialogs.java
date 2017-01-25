package com.rafaelsteil.pronouncethat;

import android.content.Context;
import android.support.v7.app.AlertDialog;

/**
 * General alerts and messages to show to the user
 */
public class Dialogs {
	/**
	 * Informs that the language pack for a given language is not installed.
	 * Asks for user's choice
	 * @param onSuccess should pass true if the user agreed to download the language pack
	 */
	public static void langNotAvailable(Context context, ActionCallback<Boolean> onSuccess) {
		new AlertDialog.Builder(context)
				.setTitle(R.string.dialog_question_title)
				.setMessage(R.string.language_pack_download_required)
				.setCancelable(true)
				.setNegativeButton(R.string.dialog_negative, (dialog, which) -> onSuccess.result(false))
				.setPositiveButton(R.string.dialog_positive, (dialog, which) -> onSuccess.result(true))
				.create()
				.show();
	}

	/**
	 * Just shows a message informing that tts is not working at all
	 */
	public static void ttsNotWorking(Context context) {
		new AlertDialog.Builder(context)
				.setTitle(R.string.dialog_error_title)
				.setMessage(R.string.tts_not_working)
				.setPositiveButton("OK", null)
				.create()
				.show();
	}

	/**
	 * Informs that tts init failed
	 */
	public static void initFailed(Context context) {
		new AlertDialog.Builder(context)
				.setTitle(R.string.dialog_error_title)
				.setMessage(R.string.tts_init_failed)
				.setPositiveButton("OK", null)
				.create()
				.show();
	}
}
