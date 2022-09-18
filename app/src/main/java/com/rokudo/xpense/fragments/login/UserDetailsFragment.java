package com.rokudo.xpense.fragments.login;

import static com.rokudo.xpense.utils.DatabaseUtils.usersRef;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.rokudo.xpense.R;
import com.rokudo.xpense.activities.MainActivity;
import com.rokudo.xpense.databinding.FragmentUserDetailsBinding;


public class UserDetailsFragment extends Fragment {
    private static final String TAG = "UserDetailsFragment";

    private FragmentUserDetailsBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentUserDetailsBinding.inflate(inflater, container, false);


        binding.doneBtn.setOnClickListener(view1 -> {
            if (binding.displayNameTextInput.getText() == null || binding.displayNameTextInput.getText().toString().trim().equals("")) {
                Toast.makeText(requireContext(), "Name cannot be empty", Toast.LENGTH_SHORT).show();
            } else {
                if (FirebaseAuth.getInstance().getCurrentUser() != null && FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber() != null) {
                    usersRef.document(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber())
                            .update("name", binding.displayNameTextInput.getText().toString())
                            .addOnSuccessListener(unused -> {
                                Intent i = new Intent(requireActivity(), MainActivity.class);
                                startActivity(i);
                                requireActivity().finish();
                            });
                }
            }

        });

        return binding.getRoot();
    }
}