package com.rokudo.xpense.fragments;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.transition.MaterialFadeThrough;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.StorageReference;
import com.rokudo.xpense.R;
import com.rokudo.xpense.databinding.FragmentSettingsBinding;
import com.rokudo.xpense.utils.DatabaseUtils;
import com.rokudo.xpense.utils.RotateBitmap;
import com.rokudo.xpense.utils.dialogs.DialogUtils;
import com.rokudo.xpense.utils.dialogs.UploadingDialog;

import java.io.ByteArrayOutputStream;
import java.util.Objects;

public class SettingsFragment extends Fragment {
    private static final String TAG = "SettingsFragment";

    private FragmentSettingsBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);

        binding.signOutBtn.setOnClickListener(view -> FirebaseAuth.getInstance().signOut());

        binding.backBtn.setOnClickListener(view -> Navigation.findNavController(binding.getRoot()).popBackStack());

        Glide.with(binding.getRoot())
                .load(DatabaseUtils.getCurrentUser().getPictureUrl())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .apply(RequestOptions.circleCropTransform())
                .placeholder(DialogUtils.getCircularProgressDrawable(requireContext()))
                .fallback(R.drawable.ic_baseline_person_24)
                .transition(withCrossFade())
                .into(binding.profilePicture);

        binding.profilePicture.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startForResultFromGallery.launch(intent);
        });

        binding.name.setText(DatabaseUtils.getCurrentUser().getName());

        return binding.getRoot();
    }

    private final ActivityResultLauncher<Intent> startForResultFromGallery = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    try {
                        if (result.getData() != null) {
                            Uri selectedImageUri = result.getData().getData();
                            Bitmap bitmap = RotateBitmap.HandleSamplingAndRotationBitmap(requireContext(), selectedImageUri);

                            StorageReference storageReference = createPictureStorageReference(selectedImageUri);

                            byte[] data = getBytesFromBitmap(bitmap);

                            uploadPicture(bitmap, storageReference, data);

                            // set bitmap to image view here........
                            setImagePicture(bitmap);
                        }
                    } catch (Exception exception) {
                        Log.d("TAG", "" + exception.getLocalizedMessage());
                    }
                }
            });

    private void setImagePicture(Bitmap bitmap) {
        Glide.with(binding.getRoot())
                .load(bitmap)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .apply(RequestOptions.circleCropTransform())
                .placeholder(DialogUtils.getCircularProgressDrawable(requireContext()))
                .fallback(R.drawable.ic_baseline_person_24)
                .transition(withCrossFade())
                .into(binding.profilePicture);
    }

    @NonNull
    private StorageReference createPictureStorageReference(Uri selectedImageUri) {
        return DatabaseUtils.userPicturesRef.child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getPhoneNumber()
                + "." + getFileExtension(selectedImageUri));
    }

    @NonNull
    private byte[] getBytesFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    private void uploadPicture(Bitmap bitmap, StorageReference storageReference, byte[] data) {
        UploadingDialog uploadingDialog = new UploadingDialog();
        uploadingDialog.show(getParentFragmentManager(), "");

        storageReference.putBytes(data).addOnSuccessListener(taskSnapshot -> storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
            String picUrl = uri.toString();
            DatabaseUtils.usersRef.document(DatabaseUtils.getCurrentUser().getPhoneNumber()).update("pictureUrl", picUrl);
            setImagePicture(bitmap);
            uploadingDialog.dismiss();
        })).addOnFailureListener(e -> Log.e(TAG, "onFailure: ", e));
    }

    private String getFileExtension(Uri selectedImageUri) {
        ContentResolver cR = requireActivity().getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(selectedImageUri));
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        MaterialFadeThrough materialContainerTransform = new MaterialFadeThrough();
        materialContainerTransform.setDuration(500);
        setSharedElementEnterTransition(materialContainerTransform);

        super.onCreate(savedInstanceState);
    }
}