package com.star1xr.treelauncher.ui.control.input;

import static android.content.Context.INPUT_METHOD_SERVICE;

import android.content.Context;
import android.util.AttributeSet;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * [From PojavLauncher](https://github.com/PojavLauncherTeam/PojavLauncher/blob/v3_openjdk/app_pojavlauncher/src/main/java/net/kdt/pojavlaunch/customcontrols/keyboard/TouchCharInput.java)
 * This class is intended for sending characters used in chat via the virtual keyboard
 */
public class TouchCharInput extends androidx.appcompat.widget.AppCompatEditText {
    public static final String TEXT_FILLER = "                              ";
    public TouchCharInput(@NonNull Context context) {
        this(context, null);
    }
    public TouchCharInput(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, androidx.appcompat.R.attr.editTextStyle);
    }
    public TouchCharInput(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setup();
    }


    private boolean mIsDoingInternalChanges = false;
    private InputListener mListener;

    public void enableKeyboard() {
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(INPUT_METHOD_SERVICE);
        enable();
        imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT);
        clear();
    }

    /**
     * Clear the EditText from any leftover inputs
     * It does not affect the in-game input
     */
    public void clear() {
        mIsDoingInternalChanges = true;
        setText(TEXT_FILLER);
        setSelection(TEXT_FILLER.length());
        mIsDoingInternalChanges = false;
    }

    /** Regain ability to exist, take focus and have some text being input */
    private void enable(){
        setEnabled(true);
        setFocusable(true);
        requestFocus();
    }

    public void setListener(InputListener listener){
        mListener = listener;
    }

    /** This function deals with anything that has to be executed when the constructor is called */
    private void setup() {
        setOnEditorActionListener((textView, i, keyEvent) -> {
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getWindowToken(), 0);
            if (mListener != null) {
                mListener.onEnter();
            }
            clear();
            return false;
        });
        enable();
        clear();
    }

    /**
     * We take the new chars, and send them to the game.
     * If less chars are present, remove some.
     * The text is always cleaned up.
     */
    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter);
        if (mIsDoingInternalChanges)
            return;
        if (mListener != null) {
            for (int i = 0; i < lengthBefore; ++i) {
                mListener.onBackspace();
            }

            for (int i = start, count = 0; count < lengthAfter; ++i) {
                mListener.onSend(text.charAt(i));
                ++count;
            }
        }

        // Reset the keyboard state
        if (text.length() < 1)
            clear();
    }
}
