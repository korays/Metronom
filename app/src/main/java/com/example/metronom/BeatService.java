package com.example.metronom;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

public class BeatService extends Service{

	public static final String TAG = "BeatService";
	private Options options;
	private static Camera camera = null;
	private Camera.Parameters parameters;
	private Vibrator vibrator;

	Messenger mServiceMessenger;
	Messenger mActivityMessenger;

	Handler mHandler = new Handler();
	Runnable runnable = new Runnable() {
		@Override
		public void run() {
			Log.w(TAG, "runnable");
			if (options.isVibtare()) {
				vibrator.vibrate(30);
			}
			if (options.isFlash()) {
				turnOnFlash();
				turnOffFlash();
			}
			if (options.isSound()) {
				ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
				toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 100);
			}

			sendMsgToActivity("beat");

			if (options.getBpm() != 0) {
				mHandler.postDelayed(this, 60000 / options.getBpm());
			}
		}
	};

	private void sendMsgToActivity(String beat) {
		Message msg = new Message();
		msg.obj = beat;
		try {
			mActivityMessenger.send(msg);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	private void startBeating() {
		stopBeating();
		runnable.run();
	}

	private void stopBeating() {
		mHandler.removeCallbacks(runnable);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		Log.w(TAG, "onStartCommand");

		mActivityMessenger = intent.getParcelableExtra("Messenger");
		vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

		getCamera();

		if (options != null) {
			if (options.isBeatOn()) {
				sendMsgToActivity("already beating");
			}
		}

		return Service.START_NOT_STICKY;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mServiceMessenger = new Messenger(new IncomingHandler());
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mHandler.removeCallbacks(runnable);
	}

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return mServiceMessenger.getBinder();
	}

	private class IncomingHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			options = (Options)msg.obj;
			if (options.isBeatOn()) {
				startBeating();
			} else {
				stopBeating();
			}
		}
	}

	private void getCamera() {
		if (camera == null) {
			try {
				camera = Camera.open();
				parameters = camera.getParameters();
			} catch (RuntimeException e) {
				Log.e(TAG, e.getMessage());
			}
		}
	}


	private void turnOnFlash() {
		if (camera == null || parameters == null) {
			return;
		}

		parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
		camera.setParameters(parameters);
//		camera.startPreview();
	}

	private void turnOffFlash() {
		if (camera == null || parameters == null) {
			return;
		}
		parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
		camera.setParameters(parameters);
		camera.stopPreview();
	}


}
