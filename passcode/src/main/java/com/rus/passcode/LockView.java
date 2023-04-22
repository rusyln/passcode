package com.rus.passcode;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.mindrot.jbcrypt.BCrypt;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class LockView extends FrameLayout implements View.OnClickListener {

    private boolean secondInput;
    private String localLockcode = "";
    private LockcodeViewListener listener;
    private ViewGroup layout_psd;
    private TextView tv_input_tip;
    private TextView number0, number1, number2, number3, number4, number5, number6, number7, number8, number9;
    private ImageView numberB, numberOK;
    private ImageView iv_lock, iv_ok;
    private View cursor;

    private String firstInputTip = "Enter a pin of 4 digits";
    private String secondInputTip = "Re-enter new pin";
    private String wrongLengthTip = "Enter a pin of 4 digits";
    private String wrongInputTip = "PIN do not match";
    private String correctInputTip = "PIN is correct";

    private int lockcodeLength = 4;
    private int correctStatusColor = 0xFF61C560; //0xFFFF0000
    private int wrongStatusColor = 0xFFF24055;
    private int normalStatusColor = 0xFFFFFFFF;
    private int numberTextColor = 0xFF747474;
    private int lockcodeType = LockcodeViewType.TYPE_SET_LOCKCODE;
    private boolean isLockEncrypted = false;

    public LockView(@NonNull Context context) {
        this(context, null);
    }

    public LockView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        inflate(getContext(), R.layout.layout_lockcode_view, this);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.LockcodeView);
        try {
            lockcodeType = typedArray.getInt(R.styleable.LockcodeView_lockedViewType, lockcodeType);
            lockcodeLength = typedArray.getInt(R.styleable.LockcodeView_lockedLength, lockcodeLength);
            normalStatusColor = typedArray.getColor(R.styleable.LockcodeView_normalStateColor, normalStatusColor);
            wrongStatusColor = typedArray.getColor(R.styleable.LockcodeView_wrongStateColor, wrongStatusColor);
            correctStatusColor = typedArray.getColor(R.styleable.LockcodeView_correctStateColor, correctStatusColor);
            numberTextColor = typedArray.getColor(R.styleable.LockcodeView_numberTextColor, numberTextColor);
            firstInputTip = typedArray.getString(R.styleable.LockcodeView_firstInputTip);
            secondInputTip = typedArray.getString(R.styleable.LockcodeView_secondInputTip);
            wrongLengthTip = typedArray.getString(R.styleable.LockcodeView_wrongLengthTip);
            wrongInputTip = typedArray.getString(R.styleable.LockcodeView_wrongInputTip);
            correctInputTip = typedArray.getString(R.styleable.LockcodeView_correctInputTip);
        } finally {
            typedArray.recycle();
        }

        firstInputTip = firstInputTip == null ? "Enter a pin of 4 digits" : firstInputTip;
        secondInputTip = secondInputTip == null ? "Re-enter new pin" : secondInputTip;
        wrongLengthTip = wrongLengthTip == null ? firstInputTip : wrongLengthTip;
        wrongInputTip = wrongInputTip == null ? "PIN do not match" : wrongInputTip;
        correctInputTip = correctInputTip == null ? "PIN is correct" : correctInputTip;

        init();
    }


    private void init() {

        layout_psd = (ViewGroup) findViewById(R.id.layout_psd);
        tv_input_tip = (TextView) findViewById(R.id.tv_input_tip);
        cursor = findViewById(R.id.cursor);
        iv_lock = (ImageView) findViewById(R.id.iv_lock);
        iv_ok = (ImageView) findViewById(R.id.iv_ok);

        tv_input_tip.setText(firstInputTip);

        number0 = (TextView) findViewById(R.id.number0);
        number1 = (TextView) findViewById(R.id.number1);
        number2 = (TextView) findViewById(R.id.number2);
        number3 = (TextView) findViewById(R.id.number3);
        number4 = (TextView) findViewById(R.id.number4);
        number5 = (TextView) findViewById(R.id.number5);
        number6 = (TextView) findViewById(R.id.number6);
        number7 = (TextView) findViewById(R.id.number7);
        number8 = (TextView) findViewById(R.id.number8);
        number9 = (TextView) findViewById(R.id.number9);
        numberOK = (ImageView) findViewById(R.id.numberOK);
        numberB = (ImageView) findViewById(R.id.numberB);

        number0.setOnClickListener(this);
        number1.setOnClickListener(this);
        number2.setOnClickListener(this);
        number3.setOnClickListener(this);
        number4.setOnClickListener(this);
        number5.setOnClickListener(this);
        number6.setOnClickListener(this);
        number7.setOnClickListener(this);
        number8.setOnClickListener(this);
        number9.setOnClickListener(this);

        numberB.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteChar();
            }
        });
        numberB.setOnLongClickListener(new OnLongClickListener() {
            @Override public boolean onLongClick(View view) {
                deleteAllChars();
                return true;
            }
        });
        numberOK.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                next();
            }
        });


        tintImageView(numberB, numberTextColor);
        tintImageView(numberOK, numberTextColor);
        tintImageView(iv_ok, correctStatusColor);

        number0.setTag(0);
        number1.setTag(1);
        number2.setTag(2);
        number3.setTag(3);
        number4.setTag(4);
        number5.setTag(5);
        number6.setTag(6);
        number7.setTag(7);
        number8.setTag(8);
        number9.setTag(9);
        tv_input_tip.setTextColor(numberTextColor);
        number0.setTextColor(numberTextColor);
        number1.setTextColor(numberTextColor);
        number2.setTextColor(numberTextColor);
        number3.setTextColor(numberTextColor);
        number4.setTextColor(numberTextColor);
        number5.setTextColor(numberTextColor);
        number6.setTextColor(numberTextColor);
        number7.setTextColor(numberTextColor);
        number8.setTextColor(numberTextColor);
        number9.setTextColor(numberTextColor);

    }

    @Override
    public void onClick(View view) {
        int number = (int) view.getTag();
        addChar(number);
        if(layout_psd.getChildCount() == lockcodeLength){
            next();
        }
    }

    public String getLocalLockcode() {
        return localLockcode;
    }

    public LockView isLockEncrypted(boolean isLockEncrypted) {
        this.isLockEncrypted = isLockEncrypted;
        return this;
    }

    /**
     * set  localLockcode
     *
     * @param localLockcode the code will to check
     */
    public LockView setLocalLockcode(String localLockcode) {
        if(! this.isLockEncrypted) {
            for (int i = 0; i < localLockcode.length(); i++) {
                char c = localLockcode.charAt(i);
                if (c < '0' || c > '9') {
                    throw new RuntimeException("must be number digit");
                }
            }
        }
        this.localLockcode = localLockcode;
        this.lockcodeType = LockcodeViewType.TYPE_CHECK_LOCKCODE;
        return this;
    }

    public LockcodeViewListener getListener() {
        return listener;
    }

    public LockView setListener(LockcodeViewListener listener) {
        this.listener = listener;
        return this;
    }

    public String getFirstInputTip() {
        return firstInputTip;
    }

    public LockView setFirstInputTip(String firstInputTip) {
        this.firstInputTip = firstInputTip;
        return this;
    }

    public String getSecondInputTip() {
        return secondInputTip;
    }

    public LockView setSecondInputTip(String secondInputTip) {
        this.secondInputTip = secondInputTip;
        return this;
    }

    public String getWrongLengthTip() {
        return wrongLengthTip;
    }

    public LockView setWrongLengthTip(String wrongLengthTip) {
        this.wrongLengthTip = wrongLengthTip;
        return this;
    }

    public String getWrongInputTip() {
        return wrongInputTip;
    }

    public LockView setWrongInputTip(String wrongInputTip) {
        this.wrongInputTip = wrongInputTip;
        return this;
    }

    public String getCorrectInputTip() {
        return correctInputTip;
    }

    public LockView setCorrectInputTip(String correctInputTip) {
        this.correctInputTip = correctInputTip;
        return this;
    }

    public int getLockcodeLength() {
        return lockcodeLength;
    }

    public LockView setLockcodeLength(int lockcodeLength) {
        this.lockcodeLength = lockcodeLength;
        return this;
    }

    public int getCorrectStatusColor() {
        return correctStatusColor;
    }

    public LockView setCorrectStatusColor(int correctStatusColor) {
        this.correctStatusColor = correctStatusColor;
        return this;
    }

    public int getWrongStatusColor() {
        return wrongStatusColor;
    }

    public LockView setWrongStatusColor(int wrongStatusColor) {
        this.wrongStatusColor = wrongStatusColor;
        return this;
    }

    public int getNormalStatusColor() {
        return normalStatusColor;
    }

    public LockView setNormalStatusColor(int normalStatusColor) {
        this.normalStatusColor = normalStatusColor;
        return this;
    }

    public int getNumberTextColor() {
        return numberTextColor;
    }

    public LockView setNumberTextColor(int numberTextColor) {
        this.numberTextColor = numberTextColor;
        return this;
    }

    public @LockcodeViewType
    int getLockcodeType() {
        return lockcodeType;
    }

    public LockView setLockcodeType(@LockcodeViewType int lockcodeType) {
        this.lockcodeType = lockcodeType;
        return this;
    }

    /**
     * <pre>
     * passcodeView.setListener(new PasscodeView.LockcodeViewListener() {
     * public void onFail() {
     * }
     *
     * public void onSuccess(String number) {
     * String encrypted = SecurePreferences.hashPrefKey(raw);
     * SharedPreferences.Editor editor = keys.edit();
     * editor.putString("passcode", encrypted);
     * editor.commit();
     * finish();
     * }
     * });
     * Second, compare using the overridden equals() method:
     *
     * class PView extends PasscodeView {
     * public PView(Context context) {
     * super(context);
     * }
     * protected boolean equals(String psd) {
     * String after = SecurePreferences.hashPrefKey(raw);
     * return after.equals(encrypted_passcode);
     * }
     * }
     * PView passcodeView = new PView(PasscodeActivity.this);
     *
     * </pre>
     * @param val the input number string
     * @return true if val is right passcode
     */
    protected boolean equals(String val) {
        if(this.isLockEncrypted) {
            return BCrypt.checkpw(val, localLockcode);
        } else {
            return localLockcode.equals(val);
        }
    }

    private void next() {
        if (lockcodeType == LockcodeViewType.TYPE_CHECK_LOCKCODE && TextUtils.isEmpty(localLockcode)) {
            throw new RuntimeException("must set localLocked when type is TYPE_CHECK_LOCKCODE");
        }

        String psd = getLockcodeFromView();
        if (psd.length() != lockcodeLength) {
            tv_input_tip.setText(wrongLengthTip);
            runTipTextAnimation();
            return;
        }

        if (lockcodeType == LockcodeViewType.TYPE_SET_LOCKCODE && !secondInput) {
            // second input
            tv_input_tip.setText(secondInputTip);
            localLockcode = psd;
            clearChar();
            secondInput = true;
            return;
        }

        if (equals(psd)) {
            // match
            runOkAnimation();
        } else {
            runWrongAnimation();
        }
    }

    private void addChar(int number) {
        if (layout_psd.getChildCount() >= lockcodeLength) {
            return;
        }
       CircleView psdView = new CircleView(getContext());
        int size = dpToPx(8);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
        params.setMargins(size, 0, size, 0);
        psdView.setLayoutParams(params);
        psdView.setColor(normalStatusColor);
        psdView.setTag(number);
        layout_psd.addView(psdView);
    }

    private int dpToPx(float valueInDp) {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, valueInDp, metrics);
    }

    private void tintImageView(ImageView imageView, int color) {
        imageView.getDrawable().mutate().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
    }

    private void clearChar() {
        layout_psd.removeAllViews();
    }

    private void deleteChar() {
        int childCount = layout_psd.getChildCount();
        if (childCount <= 0) {
            return;
        }
        layout_psd.removeViewAt(childCount - 1);
    }

    private void deleteAllChars() {
        int childCount = layout_psd.getChildCount();
        if (childCount <= 0) {
            return;
        }
        layout_psd.removeAllViews();
    }

    public void runTipTextAnimation() {
        shakeAnimator(tv_input_tip).start();
    }

    public void runWrongAnimation() {
        cursor.setTranslationX(0);
        cursor.setVisibility(VISIBLE);
        cursor.animate()
                .translationX(layout_psd.getWidth())
                .setDuration(600)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        cursor.setVisibility(INVISIBLE);
                        tv_input_tip.setText(wrongInputTip);
                        setPSDViewBackgroundResource(wrongStatusColor);
                        Animator animator = shakeAnimator(layout_psd);
                        animator.addListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                setPSDViewBackgroundResource(normalStatusColor);
                                if (secondInput && listener != null) {
                                    listener.onFail(getLockcodeFromView());
                                }
                            }
                        });
                        animator.start();
                    }
                })
                .start();
    }

    private Animator shakeAnimator(View view) {
        return ObjectAnimator
                .ofFloat(view, "translationX", 0, 25, -25, 25, -25, 15, -15, 6, -6, 0)
                .setDuration(500);
    }

    private void setPSDViewBackgroundResource(int color) {
        int childCount = layout_psd.getChildCount();
        for (int i = 0; i < childCount; i++) {
            ((CircleView) layout_psd.getChildAt(i)).setColor(color);
        }
    }

    public void runOkAnimation() {
        cursor.setTranslationX(0);
        cursor.setVisibility(VISIBLE);
        cursor.animate()
                .setDuration(600)
                .translationX(layout_psd.getWidth())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        cursor.setVisibility(INVISIBLE);
                        setPSDViewBackgroundResource(correctStatusColor);
                        tv_input_tip.setText(correctInputTip);
                        iv_lock.animate().alpha(0).scaleX(0).scaleY(0).setDuration(500).start();
                        iv_ok.animate().alpha(1).scaleX(1).scaleY(1).setDuration(500)
                                .setListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        super.onAnimationEnd(animation);
                                        if (listener != null) {
                                            listener.onSuccess(getLockcodeFromView());
                                        }
                                    }
                                }).start();
                    }
                })
                .start();

    }

    private String getLockcodeFromView() {
        StringBuilder sb = new StringBuilder();
        int childCount = layout_psd.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = layout_psd.getChildAt(i);
            int num = (int) child.getTag();
            sb.append(num);
        }
        return sb.toString();
    }

    /**
     * The type for this lockcodeView
     */
    @IntDef({LockcodeViewType.TYPE_SET_LOCKCODE, LockcodeViewType.TYPE_CHECK_LOCKCODE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface LockcodeViewType {

        /**
         * set lockcode, with twice input
         */
        int TYPE_SET_LOCKCODE = 0;

        /**
         * check lockcode, must pass the result as parameter {@link LockView#setLocalLockcode(String)}
         */
        int TYPE_CHECK_LOCKCODE = 1;
    }

    public interface LockcodeViewListener {

        void onFail(String wrongNumber);

        void onSuccess(String number);
    }
}
