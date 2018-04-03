package com.lupinemoon.favicoin.presentation.utils;

import android.app.Activity;
import android.databinding.DataBindingUtil;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;

import com.lupinemoon.favicoin.R;
import com.lupinemoon.favicoin.databinding.FragmentCustomDialogBinding;
import com.lupinemoon.favicoin.presentation.ui.base.BaseActivity;
import com.lupinemoon.favicoin.presentation.widgets.DecoratorOnClickListener;

public class DialogUtils {

    public enum AlertType {
        NONE, NETWORK, ERROR, WARNING
    }

    /**
     * A generic method that shows an alert dialog. This allows us to centrally style and manage the dialogs
     *
     * @param activity       The activity context
     * @param title          The dialog's title
     * @param message        The dialog's message
     * @param posButton      The dialog's positive button text
     * @param negButton      The dialog's negative button text
     * @param posButtonClick The dialog's positive button handler
     * @param negButtonClick The dialog's negative button handler
     * @param alertType      The type of alert. Use AlertType.NONE if not required
     */
    public static AlertDialog showCustomAlertDialog(
            @NonNull final Activity activity,
            @NonNull String title,
            @NonNull String message,
            int posButton,
            int negButton,
            View.OnClickListener posButtonClick,
            View.OnClickListener negButtonClick,
            @NonNull AlertType alertType) {

        if (((BaseActivity) activity).isBasicallyDestroyedCompat()) {
            return null;
        }

        final AlertDialog alertDialog;

        AlertDialog.Builder builder = new AlertDialog.Builder(
                activity,
                R.style.TransparentLowDimDialog);

        final FragmentCustomDialogBinding binding = DataBindingUtil.inflate(
                activity.getLayoutInflater(),
                R.layout.fragment_custom_dialog,
                null,
                false);

        builder.setView(binding.getRoot());
        builder.setCancelable(true);

        if (!TextUtils.isEmpty(title)) {
            binding.dialogTitle.setText(title);
        } else {
            binding.dialogTitle.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(message)) {
            binding.dialogMessage.setText(message);
        } else {
            binding.dialogMessage.setVisibility(View.GONE);
        }

        binding.yesButton.setVisibility(View.GONE);
        binding.noButton.setVisibility(View.GONE);

        switch (alertType) {
            case NONE:
                binding.dialogImageContainer.setVisibility(View.GONE);
                break;

            case NETWORK:
                binding.dialogImageContainer.setVisibility(View.VISIBLE);
                binding.dialogImageView.setImageResource(R.drawable.vd_no_connection);
                break;

            case ERROR:
                binding.dialogImageContainer.setVisibility(View.VISIBLE);
                binding.dialogImageView.setImageResource(R.drawable.vd_error);
                break;

            case WARNING:
                binding.dialogImageContainer.setVisibility(View.VISIBLE);
                binding.dialogImageView.setImageResource(R.drawable.vd_warning);
                break;
        }

        alertDialog = builder.show();

        if (posButton > 0 && posButtonClick != null) {
            binding.yesButtonTextView.setText(posButton);
            binding.yesButton.setVisibility(View.VISIBLE);

            DecoratorOnClickListener decoratorOnClickListener = new DecoratorOnClickListener();
            decoratorOnClickListener.add(posButtonClick);
            decoratorOnClickListener.add(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (alertDialog != null) {
                                alertDialog.dismiss();
                            }
                        }
                    }, Constants.DEFAULT_SELECTOR_DELAY);
                }
            });

            binding.yesButton.setOnClickListener(decoratorOnClickListener);
        }

        if (negButton > 0 && negButtonClick != null) {
            binding.noButtonTextView.setText(negButton);
            binding.noButton.setVisibility(View.VISIBLE);

            DecoratorOnClickListener decoratorOnClickListener = new DecoratorOnClickListener();
            decoratorOnClickListener.add(negButtonClick);
            decoratorOnClickListener.add(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (alertDialog != null && !((BaseActivity) activity).isBasicallyDestroyedCompat()) {
                                alertDialog.dismiss();
                            }
                        }
                    }, Constants.DEFAULT_SELECTOR_DELAY);
                }
            });
            binding.noButton.setOnClickListener(decoratorOnClickListener);
        }
        return alertDialog;
    }
}
