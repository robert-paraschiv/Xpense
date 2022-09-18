package com.rokudo.xpense.utils.dialogs;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.rokudo.xpense.R;
import com.rokudo.xpense.models.User;

public class PersonInfoDialogFragment extends BottomSheetDialogFragment {
    final User user;

    public PersonInfoDialogFragment(User user) {
        this.user = user;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final View dialogView = getLayoutInflater().inflate(R.layout.dialog_person_info, null);
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext(), R.style.CustomBottomSheetDialogTheme);

        bottomSheetDialog.setContentView(dialogView);
        bottomSheetDialog.show();

        ImageView profilePicture = dialogView.findViewById(R.id.profilePicture);
        TextView name = dialogView.findViewById(R.id.name);
        TextView phoneNumber = dialogView.findViewById(R.id.phoneNumber);

        Glide.with(profilePicture)
                .load(user.getPictureUrl())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .apply(RequestOptions.circleCropTransform())
                .placeholder(DialogUtils.getCircularProgressDrawable(requireContext()))
                .fallback(R.drawable.ic_baseline_person_24)
                .transition(withCrossFade())
                .into(profilePicture);

        name.setText(user.getName());
        phoneNumber.setText(user.getPhoneNumber());


        return bottomSheetDialog;
    }
}
