package com.focus.statusbar.policy;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector.OnGestureListener;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.IWindowManager;
import android.os.DeadObjectException;
import android.os.ServiceManager;
import com.focus.statusbar.R;

public class FloatSysbarService extends Service {

	private WindowManager.LayoutParams mWmParams = new WindowManager.LayoutParams();
	WindowManager mWm = null;

	View mContentLayout;
	View mFuctionMenuLayout;// fcuntion_menu

	View mSwitchmeView;
	View mPopupView;

	private int mLastMoveY, mLastMoveX;
	private int mCurMoveY, mCurMoveX;

	private int mState;
	private int mDelaytime = 1000;
	private boolean mIsPopup = true;

	/* The WindowManager capable of injecting keyStrokes. */
	final IWindowManager windowManager = IWindowManager.Stub
			.asInterface(ServiceManager.getService("window"));

	@Override
	public void onCreate() {
		super.onCreate();

		mContentLayout = LayoutInflater.from(this).inflate(R.layout.floating_sys_bar, null);

		mFuctionMenuLayout = mContentLayout.findViewById(R.id.function_menu);
		mFuctionMenuLayout.setOnTouchListener(mMovingTouchListener);

		mSwitchmeView = mContentLayout.findViewById(R.id.switchme);
		mSwitchmeView.setOnClickListener(mSysFunctionMenuClickListener);

		mPopupView = mContentLayout.findViewById(R.id.popupmenu);
		mPopupView.setOnClickListener(mSysFunctionMenuClickListener);
		mPopupView.setOnLongClickListener(mSysFunctionMenuLongClickListener);

		createView();
		mHandler.postDelayed(task, mDelaytime);
	}

	private void createView() {
		SharedPreferences shared = getSharedPreferences("float_flag",
				Activity.MODE_PRIVATE);
		SharedPreferences.Editor editor = shared.edit();
		editor.putInt("float", 1);
		editor.commit();
		mWm = (WindowManager) getApplicationContext()
				.getSystemService("window");
		mWmParams.type = 2002;
		mWmParams.flags |= 8;
		mWmParams.gravity = Gravity.LEFT | Gravity.TOP;
		mWmParams.x = 0;
		mWmParams.y = 0;
		mWmParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
		mWmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
		mWmParams.format = 1;

		mWm.addView(mContentLayout, mWmParams);

	}

	private Handler mHandler = new Handler();
	private Runnable task = new Runnable() {
		public void run() {
			// TODO Auto-generated method stub
			mHandler.postDelayed(this, mDelaytime);
			mWm.updateViewLayout(mContentLayout, mWmParams);
		}
	};

	private void updateViewPosition() {
		if (mIsPopup) {
			mWmParams.x += mCurMoveX - mLastMoveX;
		} else {
			mWmParams.x = 0;
		}
		mWmParams.y += mCurMoveY - mLastMoveY;
		mWm.updateViewLayout(mContentLayout, mWmParams);
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		popupMenu();
	}

	@Override
	public void onDestroy() {
		mHandler.removeCallbacks(task);
		mWm.removeView(mContentLayout);
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	public void popupMenu() {
		if (mIsPopup) {
			mIsPopup = false;
			mPopupView.setVisibility(View.VISIBLE);
			mFuctionMenuLayout.setVisibility(View.GONE);
			updateViewPosition();
		} else {
			mIsPopup = true;
			mPopupView.setVisibility(View.GONE);
			mFuctionMenuLayout.setVisibility(View.VISIBLE);
		}
	}

	OnTouchListener mMovingTouchListener = new OnTouchListener() {

		@Override
		public boolean onTouch(View arg0, MotionEvent event) {
			// TODO Auto-generated method stub
			int x = (int) event.getRawX();
			int y = (int) event.getRawY();
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				mState = MotionEvent.ACTION_DOWN;
				mLastMoveY = y;
				mLastMoveX = x;
				mCurMoveY = y;
				mCurMoveX = x;
				break;
			case MotionEvent.ACTION_MOVE:
				mState = MotionEvent.ACTION_MOVE;
				mLastMoveY = mCurMoveY;
				mLastMoveX = mCurMoveX;
				mCurMoveY = y;
				mCurMoveX = x;
				updateViewPosition();
				break;

			case MotionEvent.ACTION_UP:
				mState = MotionEvent.ACTION_UP;
				mLastMoveY = y;
				mLastMoveX = x;
				updateViewPosition();
				break;
			}
			return true;
		}
	};

	OnClickListener mSysFunctionMenuClickListener = new OnClickListener() {

		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			if (arg0 == mSwitchmeView || arg0 == mPopupView) {
				popupMenu();
			}
		}
	};

	OnLongClickListener mSysFunctionMenuLongClickListener = new View.OnLongClickListener() {

		@Override
        public boolean onLongClick(View v) {
			PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
			pm.goToSleep(SystemClock.uptimeMillis());
            return true;
        }
    };
}
