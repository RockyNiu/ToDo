package com.rockyniu.todolist;

import android.app.Activity;
import android.content.Context;
import android.os.IBinder;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
/**
 * Hide the soft keyboard when move action out of focus
 * @author RockyNiu
 *
 */
public class BaseActivity extends Activity{
	/**
	 * listen the touch event
	 */
	@Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {

            // get current focus, default is itemNameEditText
            View editText = getCurrentFocus();

            if (isShouldHideInput(editText, ev)) {
                hideSoftInput(editText.getWindowToken());
            }
        }
        return super.dispatchTouchEvent(ev);
    }
	
	/**
	 * Know whether click in the area of itemNameEditText
	 * @param view
	 * @param event
	 * @return true, if click itemNameEditText, return flase otherwise
	 */
	private boolean isShouldHideInput(View view, MotionEvent event) {
        if (view != null && (view instanceof EditText)) {
            int[] l = { 0, 0 };
            view.getLocationInWindow(l);
            int left = l[0], top = l[1], bottom = top + view.getHeight(), right = left
                    + view.getWidth();
            if (event.getX() > left && event.getX() < right
                    && event.getY() > top && event.getY() < bottom) {
                // click itemNameEditText
                return false;
            } else {
                return true;
            }
        }
        // not itemNameEditText
        return false;
    }
	
	/**
	 * hide soft input keyboard
	 * @param token
	 */
	private void hideSoftInput(IBinder token) {
        if (token != null) {
            InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            im.hideSoftInputFromWindow(token,
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
}
