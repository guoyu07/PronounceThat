package com.rafaelsteil.pronouncethat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
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
		setTTSLanguage();
	}

	private void setTTSLanguage() {
		engine.setLanguage(getPrefLanguage());
	}

	private void configureTextFieldHint() {
		String hint = getResources().getString(R.string.word_field_hint);
		String lang = getPrefLanguage().toLowerCase();
		hint = String.format(hint, lang);
		textField.setHint(hint);
		textField.setFloatingLabelText(hint);
	}

	@NonNull
	private String getPrefLanguage() {
		return getPrefs().getString("PrefLanguage", Locale.US.getDisplayName());
	}

	private SharedPreferences getPrefs() {
		return PreferenceManager.getDefaultSharedPreferences(this);
	}

	@Override
	protected void onDestroy() {
		engine.shutdown();
		super.onDestroy();
	}

	private void initTTS() {
		engine = new TTSEngine(this, this);
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
		Dialogs.initFailed(this);
	}

	@Override
	public void onInitSuccess() {
		setTTSLanguage();
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
	public void onLanguageDataRequired() {
		Dialogs.langNotAvailable(this, result -> {
			if (result) {
				engine.startTTSDataInstall(this);
			}
		});
	}

	private void showTTSNotWorkingMessage() {
		Dialogs.ttsNotWorking(this);
	}

	@Override
	public void onWillDownloadLanguage() {

	}

	@Override
	public void onLanguageDownloadFinished() {

	}
}
