package com.rokudo.xpense.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.android.material.shape.ShapeAppearanceModel;
import com.google.android.material.transition.MaterialContainerTransform;
import com.google.firebase.firestore.DocumentReference;
import com.rokudo.xpense.R;
import com.rokudo.xpense.data.viewmodels.WalletsViewModel;
import com.rokudo.xpense.databinding.FragmentAddWalletBinding;
import com.rokudo.xpense.models.Wallet;
import com.rokudo.xpense.utils.DatabaseUtils;

import java.util.Collections;
import java.util.Date;
import java.util.Objects;

public class AddWalletFragment extends Fragment {

    private FragmentAddWalletBinding binding;

    private WalletsViewModel walletsViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentAddWalletBinding.inflate(inflater, container, false);

        walletsViewModel = new ViewModelProvider(requireActivity()).get(WalletsViewModel.class);

        binding.walletTitleInput.requestFocus();
        binding.walletTitleInput.postDelayed(() -> {
            InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
        }, 250);

        binding.currencyDropBox.setText("RON", false);
        binding.currencyDropBox.setOnFocusChangeListener((view, b) -> {
            InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        });

        initOnClicks();

        return binding.getRoot();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        MaterialContainerTransform materialContainerTransform = new MaterialContainerTransform();
        materialContainerTransform.setDuration(getResources().getInteger(R.integer.transition_duration_millis));
        materialContainerTransform.setEndShapeAppearanceModel(new ShapeAppearanceModel().withCornerSize(18));
        materialContainerTransform.setStartShapeAppearanceModel(new ShapeAppearanceModel().withCornerSize(48));
        setSharedElementEnterTransition(materialContainerTransform);
        super.onCreate(savedInstanceState);
    }


    private void initOnClicks() {
        binding.backBtn.setOnClickListener(view -> Navigation.findNavController(binding.getRoot()).popBackStack());
        binding.saveWalletBtn.setOnClickListener(view -> addWalletToDb());
    }

    private void addWalletToDb() {
        DocumentReference documentReference = DatabaseUtils.walletsRef.document();
        Wallet wallet = new Wallet();
        wallet.setId(documentReference.getId());
        wallet.setAmount(Double.parseDouble(Objects.requireNonNull(binding.walletAmountInput.getText()).toString()));
        wallet.setCreation_date(new Date());
        wallet.setCurrency(binding.currencyDropBox.getText().toString());
        wallet.setTitle(Objects.requireNonNull(binding.walletTitleInput.getText()).toString());
        wallet.setUsers(Collections.singletonList(DatabaseUtils.getCurrentUser().getUid()));
        wallet.setCreator_id(DatabaseUtils.getCurrentUser().getUid());

        walletsViewModel.addWallet(wallet).observe(getViewLifecycleOwner(), s -> {
            if (s.equals("Success")) {
                Navigation.findNavController(binding.getRoot()).popBackStack();
            }
        });
    }
}