package com.rafaelsteil.pronouncethat;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.Fade;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.TextView;

import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.rafaelsteil.pronouncethat.R.id.settingsButton;

public class MainActivity extends AppCompatActivity implements TTSEngineListener {
	private TTSEngine engine;

	@BindView(R.id.textField)
	MaterialEditText textField;

	@BindView(R.id.pronounceButton)
	Button pronounceButton;

	@BindView(R.id.toolbar)
	Toolbar toolbar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		setupWindowAnimations();
		ButterKnife.bind(this);
		configureToolbar();
		initTTS();
		setupTextField();
	}

	private void setupTextField() {
		textField.setShowClearButton(true);
		textField.setOnEditorActionListener(this::onEditorAction);
	}

	private boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
		if (actionId == EditorInfo.IME_ACTION_GO) {
			pronounceThat();
			return true;
		}
		return false;
	}

	private void setupWindowAnimations() {
		if (Build.VERSION.SDK_INT >= 21) {
			Fade e = new Fade();
			e.setDuration(1000);
			getWindow().setExitTransition(e);
		}
	}

	private void configureToolbar() {
		setSupportActionBar(toolbar);
		toolbar.setTitle(R.string.app_name);
	}

	@Override
	protected void onResume() {
		super.onResume();
		configureTextFieldHint();
	}

	private void configureTextFieldHint() {
		String hint = getResources().getString(R.string.word_field_hint);
		String lang = PreferenceManager.getDefaultSharedPreferences(this).getString("PrefLanguage", "english").toLowerCase();
		hint = String.format(hint, lang);
		textField.setHint(hint);
		textField.setFloatingLabelText(hint);
	}

	@Override
	protected void onDestroy() {
		engine.shutdown();
		super.onDestroy();
	}

	private void initTTS() {
		engine = new TTSEngine(this, this);
		engine.setLanguage(Locale.US);
	}

	private void changePronounceButtonState(boolean enabled) {
		runOnUiThread(() -> pronounceButton.setEnabled(enabled));
	}

	@OnClick(settingsButton)
	void onSettingsButtonClicked() {
		Intent intent = new Intent(this, SettingsActivity.class);
		startActivity(intent);
	}

	@OnClick(R.id.pronounceButton)
	void onPronounceButtonClicked() {
		pronounceThat();
	}

	private void pronounceThat() {
		if (engine.isWorking()) {
			String text = textField.getText().toString();
			engine.speak(text);
		}
		else {
			showTTSNotWorkingMessage();
		}
	}

	@Override
	public void onInitFailed() {
		showInitFailedMessage();
	}

	private void showInitFailedMessage() {
		new AlertDialog.Builder(this)
				.setTitle(R.string.dialog_error_title)
				.setMessage(R.string.tts_init_failed)
				.setNeutralButton("OK", null)
				.create()
				.show();
	}

	@Override
	public void onInitSuccess() {
	}

	@Override
	public void onPronounceStart() {
		changePronounceButtonState(false);
	}

	@Override
	public void onPronounceFinish() {
		changePronounceButtonState(true);
	}

	@Override
	public void onLanguageDataRequired(ActionCallback<Boolean> onSuccess) {
		new AlertDialog.Builder(this)
				.setTitle(R.string.dialog_question_title)
				.setMessage(R.string.language_pack_download_required)
				.setCancelable(true)
				.setNegativeButton(R.string.dialog_negative, null)
				.setPositiveButton(R.string.dialog_positive, (dialog, which) -> onSuccess.result(true))
				.create()
				.show();
	}

	private void showTTSNotWorkingMessage() {
		new AlertDialog.Builder(this)
				.setTitle(R.string.dialog_error_title)
				.setMessage(R.string.tts_not_working)
				.setNeutralButton("OK", null)
				.create()
				.show();
	}

	@Override
	public void onWillDownloadLanguage() {

	}

	@Override
	public void onLanguageDownloadFinished() {

	}
}
