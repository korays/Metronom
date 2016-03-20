package com.example.metronom;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;

public class MainActivity extends AppCompatActivity {

	public static final String PREFS = "myPreferences";
	public static final String VIBRATE = "vibrate";
	public static final String FLASH = "flash";
	public static final String SOUND = "sound";
	public static final String BPM = "bpm";

	private Button btnVib, btnFlash, btnSound;
	private ImageView indicatorOn;

	private Options options;

	private Drawable vibrateOn, vibrateOff, flashOn, flashOff, soundOn, soundOff;
	private Intent beatIntent;
	private boolean beating;

	private MyServiceConnection mCon = new MyServiceConnection();
	private Messenger mServiceMessenger;
	private Button startButton;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		options = new Options();
		SharedPreferences prefs = this.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
		/*isVibrate = prefs.getBoolean(VIBRATE, false);
		isFlash = prefs.getBoolean(FLASH, false);
		isSound = prefs.getBoolean(SOUND, false);
		bpm = prefs.getInt(BPM, 100);*/

		options.setIsVibtare(prefs.getBoolean(VIBRATE, false));
		options.setIsFlash(prefs.getBoolean(FLASH, false));
		options.setIsSound(prefs.getBoolean(SOUND, false));
		options.setBpm(prefs.getInt(BPM, 100));


		vibrateOn = ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_vibration_24dp);
		vibrateOff = ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_smartphone_24dp);

		flashOn = ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_flash_on_24dp);
		flashOff = ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_flash_off_24dp);

		soundOn = ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_volume_up_24dp);
		soundOff = ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_volume_off_24dp);


		btnVib = (Button) findViewById(R.id.vibration);
		btnVib.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
//				isVibrate = !isVibrate;
				options.setIsVibtare(!options.isVibtare());
				vibrateToggle();
			}
		});
		btnFlash = (Button) findViewById(R.id.flash);
		btnFlash.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
//				isFlash = !isFlash;
				options.setIsFlash(!options.isFlash());
				flashToggle();
			}
		});
		btnSound = (Button) findViewById(R.id.sound);
		btnSound.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
//				isSound = !isSound;
				options.setIsSound(!options.isSound());
				soundToggle();
			}
		});

		indicatorOn = (ImageView) findViewById(R.id.indicatorOn);

		final Messenger mActivityMessenger;
		mActivityMessenger = new Messenger(new IncomingHandler());



		beatIntent = new Intent(MainActivity.this, BeatService.class);
		beatIntent.putExtra("Messenger", mActivityMessenger);
		startService(beatIntent);

		startButton = (Button) findViewById(R.id.startButton);
		assert startButton != null;
		startButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!beating) {
					startButton.setText(R.string.stop);
					beating = true;

					options.setBeatOn(true);

				} else {
					startButton.setText(R.string.start);
					options.setBeatOn(false);
					beating = false;
				}
				sendOptionsToService();
			}
		});


		final EditText seekbarValue = (EditText) findViewById(R.id.seekbarValue);
		final SeekBar seekbar = (SeekBar) findViewById(R.id.seekbar);

		assert seekbar != null;
		seekbar.setMax(200);
		seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				assert seekbarValue != null;
				seekbarValue.setText(String.valueOf(progress));
				seekbarValue.setSelection(String.valueOf(progress).length());

				if (progress==0) {
					seekBar.setProgress(1);
				}
				options.setBpm(seekBar.getProgress());
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				sendOptionsToService();
			}
		});

		assert seekbarValue != null;
		seekbarValue.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (s.length() > 0) {
					seekbar.setProgress(Integer.parseInt(String.valueOf(s)));
				} else {
					seekbar.setProgress(0);
				}
//
			}

			@Override
			public void afterTextChanged(Editable s) {}
		});

		Button minus = (Button) findViewById(R.id.minusBtn);
		Button plus = (Button) findViewById(R.id.plusBtn);

		assert minus != null;
		minus.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				seekbar.setProgress(seekbar.getProgress() - 1);
			}
		});

		assert plus != null;
		plus.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				seekbar.setProgress(seekbar.getProgress() + 1);
			}
		});


		vibrateToggle();
		flashToggle();
		soundToggle();
		seekbar.setProgress(options.getBpm());


	}

	private void sendOptionsToService() {
		Message msg = new Message();
		msg.obj = options;
		try {
			mServiceMessenger.send(msg);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	private void vibrateToggle() {
		if (options.isVibtare()) {
			btnVib.setCompoundDrawablesWithIntrinsicBounds(null, vibrateOn , null, null);
		} else {
			btnVib.setCompoundDrawablesWithIntrinsicBounds(null, vibrateOff , null, null);
		}
	}
	private void flashToggle() {
		if (options.isFlash()) {
			btnFlash.setCompoundDrawablesWithIntrinsicBounds(null, flashOn , null, null);
		} else {
			btnFlash.setCompoundDrawablesWithIntrinsicBounds(null, flashOff , null, null);
		}
	}
	private void soundToggle() {
		if (options.isSound()) {
			btnSound.setCompoundDrawablesWithIntrinsicBounds(null, soundOn , null, null);
		} else {
			btnSound.setCompoundDrawablesWithIntrinsicBounds(null, soundOff , null, null);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		Intent lIntent = new Intent(MainActivity.this, BeatService.class);
		bindService(lIntent, mCon, 0);
	}

	@Override
	protected void onPause() {
		super.onPause();
		unbindService(mCon);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		SharedPreferences.Editor editor = this.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit();
		editor.putBoolean(VIBRATE, options.isVibtare());
		editor.putBoolean(FLASH, options.isFlash());
		editor.putBoolean(SOUND, options.isSound());
		editor.putInt(BPM, options.getBpm());
		editor.commit();
	}


	private class IncomingHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			String message = (String)msg.obj;

			if (message.equals("beat")) {
				indicatorBlink();
			} else if (message.equals("already beating")) {
				startButton.setText(R.string.stop);
				beating = true;
			}
		}
	}

	private void indicatorBlink() {
		indicatorOn.setAlpha(1.0f);
		indicatorOn.animate()
				.setDuration(50)
				.alpha(0f);
	}

	class MyServiceConnection implements ServiceConnection {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mServiceMessenger = new Messenger(service);
			// where mServiceMessenger is used to send messages to Service
			// service is the binder returned from onBind method in the Service
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mServiceMessenger = null;
			unbindService(mCon);
		}
	}

}
