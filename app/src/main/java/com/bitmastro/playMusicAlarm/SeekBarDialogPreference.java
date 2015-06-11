package com.bitmastro.playMusicAlarm;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.SeekBar;
import android.widget.TextView;

/**
 * A {@link DialogPreference} that provides a user with the means to select an
 * integer from a {@link NumberPicker}, and persist it.
 * 
 * @author lukehorvat
 * 
 */
public class SeekBarDialogPreference extends DialogPreference {
	private static final int DEFAULT_MIN_VALUE = 0;
	private static final int DEFAULT_MAX_VALUE = 100;
	private static final int DEFAULT_STEP = 1;
	private static final int DEFAULT_VALUE = 0;

	private int mMinValue;
	private int mMaxValue;
	private int mValue;
	private SeekBar mSeekbar;
	private String[] valueSets;
	private int mStep;

	public SeekBarDialogPreference(Context context) {
		this(context, null);
	}

	public SeekBarDialogPreference(Context context, AttributeSet attrs) {
		super(context, attrs);

		// get attributes specified in XML
		TypedArray a = context.getTheme().obtainStyledAttributes(attrs,
				R.styleable.SeekBarDialogPreference, 0, 0);
		try {
			setMinValue(a.getInteger(R.styleable.SeekBarDialogPreference_min,
					DEFAULT_MIN_VALUE));
			setMaxValue(a.getInteger(
					R.styleable.SeekBarDialogPreference_android_max,
					DEFAULT_MAX_VALUE));
			setStep(a.getInteger(R.styleable.SeekBarDialogPreference_step,
					DEFAULT_STEP));
		} finally {
			a.recycle();
		}
		mStep = getStep();
		valueSets = new String[getMaxValue() - getMinValue() + 1];

		for (int i = 0; i < valueSets.length; i++) {

			valueSets[i] = String.valueOf((i + 1) * mStep);
		}
		// set layout
		setDialogLayoutResource(R.layout.preference_seekbar_dialog);
		setPositiveButtonText(android.R.string.ok);
		setNegativeButtonText(android.R.string.cancel);
		setDialogIcon(null);
	}

	@Override
	protected void onSetInitialValue(boolean restore, Object defaultValue) {
		setValue(restore ? getPersistedInt(DEFAULT_VALUE)
				: (Integer) defaultValue);
		setSummary(getSummary());
	}

	@Override
	protected Object onGetDefaultValue(TypedArray a, int index) {
		return a.getInt(index, DEFAULT_VALUE);
	}

	@Override
	protected View onCreateDialogView() {
		// TODO Auto-generated method stub
		View view = super.onCreateDialogView();
		mSeekbar = (SeekBar) view.findViewById(R.id.seekbar);

		mSeekbar.setMax((getMaxValue() - getMinValue()) / mStep);
		return view;
	}

	@Override
	protected void onBindDialogView(View view) {
		super.onBindDialogView(view);

		TextView dialogMessageText = (TextView) view
				.findViewById(R.id.text_dialog_message);
		dialogMessageText.setText(getDialogMessage());
		mSeekbar.setProgress((mValue - getMinValue()) / mStep);
	}

	public int getMinValue() {
		return mMinValue;
	}

	public void setMinValue(int minValue) {
		mMinValue = minValue;
		setValue(Math.max(mValue, mMinValue));
	}

	public int getMaxValue() {
		return mMaxValue;
	}

	public void setMaxValue(int maxValue) {
		mMaxValue = maxValue;
		setValue(Math.min(mValue, mMaxValue));
	}

	public int getStep() {
		return mStep;
	}

	public void setStep(int step) {
		mStep = step;
		setValue(mValue / step * step);
	}

	public int getValue() {
		return mValue;
	}

	public void setValue(int value) {
		value = Math.max(Math.min(value, mMaxValue), mMinValue);

		if (value != mValue) {
			mValue = value;
			persistInt(value);
			notifyChanged();
		}
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		super.onDialogClosed(positiveResult);

		// when the user selects "OK", persist the new value
		if (positiveResult) {
			int numberPickerValue = mSeekbar.getProgress() * mStep
					+ getMinValue();
			setSummary(getSummary());
			if (callChangeListener(numberPickerValue)) {
				setValue(numberPickerValue);
			}

		}
	}

	@Override
	protected Parcelable onSaveInstanceState() {
		// save the instance state so that it will survive screen orientation
		// changes and other events that may temporarily destroy it
		final Parcelable superState = super.onSaveInstanceState();

		// set the state's value with the class member that holds current
		// setting value
		final SavedState myState = new SavedState(superState);
		myState.value = getValue();

		return myState;
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		// check whether we saved the state in onSaveInstanceState()
		if (state == null || !state.getClass().equals(SavedState.class)) {
			// didn't save the state, so call superclass
			super.onRestoreInstanceState(state);
			return;
		}

		// restore the state
		SavedState myState = (SavedState) state;
		setValue(myState.value);

		super.onRestoreInstanceState(myState.getSuperState());
	}

	private static class SavedState extends BaseSavedState {
		int value;

		public SavedState(Parcelable superState) {
			super(superState);
		}

		public SavedState(Parcel source) {
			super(source);
			value = source.readInt();
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			super.writeToParcel(dest, flags);
			dest.writeInt(value);
		}

		@SuppressWarnings("unused")
		public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
			@Override
			public SavedState createFromParcel(Parcel in) {
				return new SavedState(in);
			}

			@Override
			public SavedState[] newArray(int size) {
				return new SavedState[size];
			}
		};
	}

	@SuppressLint("DefaultLocale")
	@Override
	public CharSequence getSummary() {
		return String.format("%d", mValue);
	}
}