package com.rokudo.xpense.fragments;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.transition.MaterialFadeThrough;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.StorageReference;
import com.rokudo.xpense.R;
import com.rokudo.xpense.adapters.InvitationAdapter;
import com.rokudo.xpense.data.viewmodels.InvitesViewModel;
import com.rokudo.xpense.data.viewmodels.TransactionViewModel;
import com.rokudo.xpense.data.viewmodels.WalletsViewModel;
import com.rokudo.xpense.databinding.FragmentSettingsBinding;
import com.rokudo.xpense.models.Invitation;
import com.rokudo.xpense.utils.DatabaseUtils;
import com.rokudo.xpense.utils.PrefsUtils;
import com.rokudo.xpense.utils.RotateBitmap;
import com.rokudo.xpense.utils.dialogs.DialogUtils;
import com.rokudo.xpense.utils.dialogs.UploadingDialog;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SettingsFragment extends Fragment implements InvitationAdapter.InvitationClickListener {
    private static final String TAG = "SettingsFragment";

    private InvitesViewModel invitesViewModel;
    private FragmentSettingsBinding binding;
    private InvitationAdapter adapter;
    private final List<Invitation> invitationList = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);

        invitesViewModel = new ViewModelProvider(requireActivity()).get(InvitesViewModel.class);

        initOnClicks();
        initViews();
        buildRv();
        loadInvitations();

        return binding.getRoot();
    }

    private void loadInvitations() {
        invitesViewModel.loadInvitations().observe(getViewLifecycleOwner(), values -> {
            if (values == null || values.isEmpty()) {
                Log.d(TAG, "loadInvitations: empty or null");
            } else {
                for (Invitation invitation : values) {
                    if (invitationList.contains(invitation)) {
                        if (invitation.getStatus().equals(Invitation.STATUS_ACCEPTED)
                                || invitation.getStatus().equals(Invitation.STATUS_DECLINED)) {
                            adapter.notifyItemRemoved(invitationList.indexOf(invitation));
                            invitationList.remove(invitation);

                        } else {
                            invitationList.set(invitationList.indexOf(invitation), invitation);
                            adapter.notifyItemChanged(invitationList.indexOf(invitation));
                        }
                    } else {
                        if (invitation.getStatus().equals(Invitation.STATUS_ACCEPTED)
                                || invitation.getStatus().equals(Invitation.STATUS_DECLINED)) {
                            continue;
                        }
                        invitationList.add(invitation);
                        adapter.notifyItemInserted(invitationList.indexOf(invitation));
                    }
                }
            }
        });
    }

    private void buildRv() {
        adapter = new InvitationAdapter(invitationList);
        binding.invitationsRv.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.invitationsRv.setAdapter(adapter);
        adapter.setInvitationClickListener(this);
    }

    private void initViews() {
        Glide.with(binding.getRoot())
                .load(DatabaseUtils.getCurrentUser().getPictureUrl())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .apply(RequestOptions.circleCropTransform())
                .placeholder(DialogUtils.getCircularProgressDrawable(requireContext()))
                .fallback(R.drawable.ic_baseline_person_24)
                .transition(withCrossFade())
                .into(binding.profilePicture);


        binding.name.setText(DatabaseUtils.getCurrentUser().getName());
    }

    private void initOnClicks() {
        binding.signOutBtn.setOnClickListener(view -> {
            deleteViewModelsData();
            FirebaseAuth.getInstance().signOut();
        });

        binding.backBtn.setOnClickListener(view -> Navigation.findNavController(binding.getRoot()).popBackStack());

        binding.profilePicture.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startForResultFromGallery.launch(intent);
        });
    }

    private void deleteViewModelsData() {
        TransactionViewModel transactionViewModel = new ViewModelProvider(requireActivity()).get(TransactionViewModel.class);
        WalletsViewModel walletsViewModel = new ViewModelProvider(requireActivity()).get(WalletsViewModel.class);

        transactionViewModel.removeAllData();
        walletsViewModel.removeAllData();
        PrefsUtils.setSelectedWalletId(requireContext(), null);
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
        UploadingDialog uploadingDialog = new UploadingDialog("Uploading, please wait...");
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
        materialContainerTransform.setDuration(getResources().getInteger(R.integer.transition_duration_millis));
        setSharedElementEnterTransition(materialContainerTransform);

        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAcceptClick(Invitation invitation) {
        invitesViewModel.updateStatus(invitation.getId(), Invitation.STATUS_ACCEPTED);
    }

    @Override
    public void onDeclineClick(Invitation invitation) {
        invitesViewModel.updateStatus(invitation.getId(), Invitation.STATUS_DECLINED);
    }
}