package com.rokudo.xpense.fragments;

import static com.rokudo.xpense.utils.DatabaseUtils.usersRef;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.rokudo.xpense.R;
import com.rokudo.xpense.adapters.ContactsAdapter;
import com.rokudo.xpense.databinding.FragmentContactsBinding;
import com.rokudo.xpense.models.Invitation;
import com.rokudo.xpense.models.User;
import com.rokudo.xpense.models.Wallet;
import com.rokudo.xpense.utils.DatabaseUtils;
import com.rokudo.xpense.utils.dialogs.TimedDialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class ContactsFragment extends Fragment implements ContactsAdapter.OnContactClickListener {
    private static final String TAG = "ContactsFragment";

    private FragmentContactsBinding binding;
    private final List<User> contactsWithApp = new ArrayList<>();
    private ContactsAdapter adapter;
    private Wallet mWallet;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentContactsBinding.inflate(inflater, container, false);

        getWalletPassed();
        initOnClicks();
        checkPermissions();
        buildRv();

        return binding.getRoot();
    }

    private void getWalletPassed() {
        if (getArguments() == null) {
            Toast.makeText(requireContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
            Navigation.findNavController(binding.getRoot()).popBackStack();
            return;
        }
        TimedDialog timedDialog = new TimedDialog("Please note that in order for a contact to be displayed, the phone number should start with +40 and  the user must have an existing Xpense account",
                4500);
        timedDialog.show(getParentFragmentManager(), "warning");
        timedDialog.startAnimation();
        new Handler().postDelayed(timedDialog::dismiss, 4500);
        mWallet = ContactsFragmentArgs.fromBundle(requireArguments()).getWallet();
    }

    private void initOnClicks() {
        binding.backBtn.setOnClickListener(v -> Navigation.findNavController(binding.getRoot()).popBackStack());
        binding.refreshBtn.setOnClickListener(v -> checkPermissions());
    }

    private void buildRv() {
        adapter = new ContactsAdapter(contactsWithApp);
        binding.contactsRV.setLayoutManager(new LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false));
        binding.contactsRV.setAdapter(adapter);
        adapter.setOnItemClickListener(this);
    }


    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            getContacts();
        } else {
            mPermissionResult.launch(Manifest.permission.READ_CONTACTS);
        }
    }

    private final ActivityResultLauncher<String> mPermissionResult = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            result -> {
                if (result) {
                    Log.e(TAG, "onActivityResult: PERMISSION GRANTED");
                    getContacts();
                } else {
                    Log.e(TAG, "onActivityResult: PERMISSION DENIED");
                    Toast.makeText(requireContext(), "Cannot get contacts without permission", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(binding.getRoot()).popBackStack();
                }
            });

    @SuppressLint("Range")
    private void getContacts() {
        List<String> contactsNumberList = new ArrayList<>();
        final ContentResolver cr = requireActivity().getContentResolver();
        String[] projection = new String[]{ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER};
        final Cursor cursor = cr.query(ContactsContract.Data.CONTENT_URI, projection, null, null, null);
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                String phoneNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                if (phoneNumber == null || !phoneNumber.startsWith("+") || phoneNumber.length() < 10) {
                    continue;
                }
                String trimmedPhoneNumber = phoneNumber.replace(" ", "");
                if (!contactsNumberList.contains(trimmedPhoneNumber)) {
                    contactsNumberList.add(trimmedPhoneNumber);
                }
            }
        }
        cursor.close();
        getContactsWithApp(contactsNumberList);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void getContactsWithApp(List<String> contactsNumberList) {
        List<DocumentReference> listDocRef = new ArrayList<>();
        for (int i = 0; i < contactsNumberList.size(); i++) {
            listDocRef.add(usersRef.document(contactsNumberList.get(i)));
        }
        List<Task<DocumentSnapshot>> tasks = new ArrayList<>();
        for (DocumentReference documentReference : listDocRef) {
            tasks.add(documentReference.get());
        }

        Tasks.whenAllComplete(tasks).addOnCompleteListener(task -> {
            tasks.stream().filter(Task::isSuccessful).forEach(task1 -> {
                DocumentSnapshot documentSnapshot = task1.getResult();
                if (documentSnapshot.exists()) {
                    try {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null && !contactsWithApp.contains(user)) {
                            contactsWithApp.add(user);
                        }
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                    }
                }
            });
            Collections.sort(contactsWithApp);
            adapter.notifyDataSetChanged();
            startPostponedEnterTransition();
        });
    }

    @Override
    public void onClick(User user) {
        Invitation invitation = createInvitation(user);

        TimedDialog uploadingDialog = new TimedDialog("Sending invitation to " + user.getName() + " ...",
                1500);
        uploadingDialog.show(getParentFragmentManager(), "sentInvite");

        DatabaseUtils.invitationsRef.document(invitation.getId())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.getResult().exists()) {
                        showInvitationAlreadySentError(uploadingDialog);
                    } else {
                        sendInvitation(invitation, uploadingDialog);
                    }
                });
    }

    @NonNull
    private Invitation createInvitation(User user) {
        Invitation invitation = new Invitation();
        invitation.setId(mWallet == null ? "" : mWallet.getId());
        invitation.setWallet_title(mWallet.getTitle());
        invitation.setStatus(Invitation.STATUS_SENT);
        invitation.setDate(new Date());
        invitation.setCreator_id(DatabaseUtils.getCurrentUser().getUid());
        invitation.setCreator_name(DatabaseUtils.getCurrentUser().getName());
        invitation.setCreator_pic_url(DatabaseUtils.getCurrentUser().getPictureUrl());
        invitation.setInvited_person_phone_number(user.getPhoneNumber());
        return invitation;
    }

    private void sendInvitation(Invitation invitation, TimedDialog uploadingDialog) {
        DatabaseUtils.invitationsRef.document(invitation.getId())
                .set(invitation)
                .addOnSuccessListener(unused -> {
                    uploadingDialog.startAnimation();

                    binding.getRoot().postDelayed(() -> {
                        uploadingDialog.dismiss();
                        Navigation.findNavController(binding.getRoot()).popBackStack(R.id.homeFragment, false);
                    }, 1500);
                });
    }

    private void showInvitationAlreadySentError(TimedDialog uploadingDialog) {
        Toast.makeText(requireContext(),
                "A user has already been invited to this wallet",
                Toast.LENGTH_LONG).show();
        uploadingDialog.dismiss();
        Navigation.findNavController(binding.getRoot()).popBackStack(R.id.homeFragment, false);
    }
}