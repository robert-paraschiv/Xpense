package com.rokudo.xpense.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rokudo.xpense.R;
import com.rokudo.xpense.databinding.FragmentContactsBinding;

public class ContactsFragment extends Fragment {
    private static final String TAG = "ContactsFragment";

    private FragmentContactsBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentContactsBinding.inflate(inflater, container, false);



        return binding.getRoot();
    }
}