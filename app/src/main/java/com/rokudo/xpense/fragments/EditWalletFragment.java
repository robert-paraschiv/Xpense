package com.rokudo.xpense.fragments;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;
import static com.rokudo.xpense.models.WalletUser.getOtherWalletUser;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.transition.MaterialSharedAxis;
import com.google.firebase.firestore.DocumentReference;
import com.rokudo.xpense.R;
import com.rokudo.xpense.data.viewmodels.WalletsViewModel;
import com.rokudo.xpense.databinding.FragmentEditWalletBinding;
import com.rokudo.xpense.models.Wallet;
import com.rokudo.xpense.models.WalletUser;
import com.rokudo.xpense.utils.DatabaseUtils;
import com.rokudo.xpense.utils.PrefsUtils;
import com.rokudo.xpense.utils.dialogs.DialogUtils;

import java.util.Collections;
import java.util.Date;
import java.util.Objects;

public class EditWalletFragment extends Fragment {

    private FragmentEditWalletBinding binding;
    private WalletsViewModel walletsViewModel;
    private Wallet mWallet;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentEditWalletBinding.inflate(inflater, container, false);

        walletsViewModel = new ViewModelProvider(requireActivity()).get(WalletsViewModel.class);

        initOnClicks();
        handleArgs();

        return binding.getRoot();
    }

    @SuppressLint("SetTextI18n")
    private void handleArgs() {
        EditWalletFragmentArgs args = EditWalletFragmentArgs.fromBundle(requireArguments());
        mWallet = args.getWallet();

        if (mWallet == null) {
            binding.fragmentTitleTv.setText("Add Wallet");
            binding.invitedPersonCard.setVisibility(View.GONE);
        } else {
            binding.invitedPersonCard.setVisibility(View.VISIBLE);
            binding.fragmentTitleTv.setText("Edit Wallet");
            binding.walletTitleInput.setText(mWallet.getTitle() == null ? "" : mWallet.getTitle());
            binding.currencyDropBox.setText(mWallet.getCurrency() == null ? "" : mWallet.getCurrency());
            binding.walletAmountInput.setText(mWallet.getAmount() == null ? "" : mWallet.getAmount() + "");
            if (mWallet.getWalletUsers() != null && mWallet.getWalletUsers().size() > 1) {
                WalletUser otherUser = getOtherWalletUser(mWallet.getWalletUsers());

                binding.invitedPersonName.setText(otherUser.getUserName() == null ? "" : otherUser.getUserName());

                Glide.with(requireContext())
                        .load(otherUser.getUserPic())
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .apply(RequestOptions.circleCropTransform())
                        .placeholder(DialogUtils.getCircularProgressDrawable(requireContext()))
                        .fallback(R.drawable.ic_baseline_person_24)
                        .error(R.drawable.ic_baseline_person_24)
                        .transition(withCrossFade())
                        .into(binding.invitedPersonPic);
            }
        }
    }

    private void initOnClicks() {
        binding.backBtn.setOnClickListener(view -> Navigation.findNavController(binding.getRoot()).popBackStack());
        binding.saveWalletBtn.setOnClickListener(view -> {
            if (mWallet == null) {
                addWalletToDb();
            } else {
                updateWallet();
            }
        });
        binding.invitedPersonCard.setOnClickListener(v -> Navigation.findNavController(binding.getRoot())
                .navigate(EditWalletFragmentDirections.actionEditWalletFragmentToContactsFragment(mWallet)));
    }

    private void updateWallet() {

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

        PrefsUtils.setSelectedWalletId(requireContext(), wallet.getId());
        walletsViewModel.addWallet(wallet).observe(getViewLifecycleOwner(), s -> {
            if (s.equals("Success")) {
                Navigation.findNavController(binding.getRoot()).popBackStack();
            }
        });
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MaterialSharedAxis enter = new MaterialSharedAxis(MaterialSharedAxis.X, false);
        enter.setDuration(getResources().getInteger(R.integer.transition_duration_millis));
        MaterialSharedAxis exit = new MaterialSharedAxis(MaterialSharedAxis.X, true);
        exit.setDuration(getResources().getInteger(R.integer.transition_duration_millis));
        setEnterTransition(enter);
        setReturnTransition(exit);
    }
}