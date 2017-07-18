package com.philliphsu.numberpadtimepicker;

import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;

public class BottomSheetNumberPadTimePickerDialog extends BottomSheetDialog {

    private final NumberPadTimePickerDialogViewDelegate mViewDelegate;
    private final BottomSheetNumberPadTimePickerDialogThemer mThemer;
    private final BottomSheetBehavior<? extends View> mBottomSheetBehavior;

    public BottomSheetNumberPadTimePickerDialog(@NonNull Context context,
            @Nullable OnTimeSetListener listener, boolean is24HourMode) {
        this(context, 0, listener, is24HourMode);
    }

    public BottomSheetNumberPadTimePickerDialog(@NonNull Context context, @StyleRes int themeResId,
            @Nullable OnTimeSetListener listener, boolean is24HourMode) {
        super(context, resolveDialogTheme(context, themeResId));
        // This is inflated via the LayoutInflater from this Dialog's Window, which was created
        // with this Dialog's Context. If the Dialog's Context is themed, then the hierarchy
        // inflated is sure to be themed appropriately.
        final View root = getLayoutInflater().inflate(
                R.layout.nptp_bottomsheet_numberpad_time_picker_dialog, null);
        final NumberPadTimePicker timePicker = (NumberPadTimePicker)
                root.findViewById(R.id.nptp_time_picker);
        final NumberPadTimePickerBottomSheetComponent timePickerComponent =
                (NumberPadTimePickerBottomSheetComponent) timePicker.getComponent();

        mViewDelegate = new NumberPadTimePickerDialogViewDelegate(this,
                // Prefer getContext() over the provided Context because the Context
                // that the Dialog runs in may not be the same as the provided Context.
                // Follow the chain of construction starting from the call to super().
                // The penultimate call is
                /**    {@link android.app.Dialog#Dialog(Context, int)} */
                // and if we run through its implementation, we eventually see
                /**    {@link android.view.ContextThemeWrapper} */
                // is used as the Dialog's Context. It is a "context wrapper that allows
                // you to modify or replace the theme of the wrapped context", and it
                // works by applying the specified theme on top of the base context's theme.
                getContext(), timePicker, timePickerComponent.getOkButton(),
                null /* cancel button */, listener, is24HourMode);
        setContentView(root);

        mThemer = new BottomSheetNumberPadTimePickerDialogThemer(timePickerComponent);
        mBottomSheetBehavior = BottomSheetBehavior.from((View) root.getParent());

        mBottomSheetBehavior.setPeekHeight(getContext().getResources().getDimensionPixelSize(
                R.dimen.nptp_bottom_sheet_grid_picker_peek_height));
        // Overrides the default callback, but we kept the default behavior.
        mBottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_HIDDEN:
                        cancel();  // Default behavior
                        break;
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        // Do not allow collapsing
                        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {}
        });
    }

    public BottomSheetNumberPadTimePickerDialogThemer getThemer() {
        return mThemer;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Override the dialog's width if we're running in an eligible layout qualifier.
        try {
            getWindow().setLayout(getContext().getResources().getDimensionPixelSize(
                    R.dimen.nptp_bottom_sheet_dialog_width), ViewGroup.LayoutParams.WRAP_CONTENT);
        } catch (Resources.NotFoundException nfe) {
            // Do nothing.
        }
        mViewDelegate.onCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    @NonNull
    @Override
    public Bundle onSaveInstanceState() {
        return mViewDelegate.onSaveInstanceState(super.onSaveInstanceState());
    }

    @Override
    protected void onStop() {
        super.onStop();
        mViewDelegate.onStop();
    }

    static int resolveDialogTheme(Context context, int resId) {
        if (resId == 0) {
            final TypedValue outValue = new TypedValue();
            context.getTheme().resolveAttribute(R.attr.nptp_numberPadTimePickerBottomSheetDialogTheme,
                    outValue, true);
            return outValue.resourceId;
        } else {
            return resId;
        }
    }
}
