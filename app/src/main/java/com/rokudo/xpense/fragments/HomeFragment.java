package com.rokudo.xpense.fragments;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;
import static com.rokudo.xpense.utils.DatabaseUtils.usersRef;
import static com.rokudo.xpense.utils.UserUtils.checkIfUserPicIsDifferent;
import static com.rokudo.xpense.utils.dialogs.DialogUtils.getCircularProgressDrawable;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.FragmentNavigator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.transition.MaterialElevationScale;
import com.google.android.material.transition.MaterialFadeThrough;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.ListenerRegistration;
import com.rokudo.xpense.R;
import com.rokudo.xpense.adapters.TransactionsAdapter;
import com.rokudo.xpense.databinding.FragmentHomeBinding;
import com.rokudo.xpense.models.Transaction;
import com.rokudo.xpense.models.User;
import com.rokudo.xpense.utils.DatabaseUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";

    private FragmentHomeBinding binding;
    private List<Transaction> transactionList = new ArrayList<>();

    private TransactionsAdapter adapter;

    private ListenerRegistration userDetailsListenerRegistration;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (binding == null) {
            binding = FragmentHomeBinding.inflate(inflater, container, false);
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
//                convViewModel = new ViewModelProvider(this).get(ConvViewModel.class);
            }
            initOnClicks();
            buildRecyclerView();
            initializeDummyRv();
        }
        return binding.getRoot();
    }

    private void initializeDummyRv() {
        transactionList.add(new Transaction("Transportation", 0.235, new Date(), ""));
        transactionList.add(new Transaction("Transportation", 0.235, new Date(), ""));
        transactionList.add(new Transaction("Transportation", 0.235, new Date(), ""));
        transactionList.add(new Transaction("Transportation", 0.235, new Date(), ""));
        transactionList.add(new Transaction("Transportation", 0.235, new Date(), ""));
        transactionList.add(new Transaction("Transportation", 0.235, new Date(), ""));
        transactionList.add(new Transaction("Transportation", 0.235, new Date(), ""));
        adapter.notifyDataSetChanged();
    }

    private void buildRecyclerView() {
        adapter = new TransactionsAdapter(transactionList);
        binding.recentTransactionsRv.setLayoutManager(new LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false));
        binding.recentTransactionsRv.setAdapter(adapter);
//        adapter.setOnItemClickListener();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (userDetailsListenerRegistration != null) {
            userDetailsListenerRegistration.remove();
        }
        getUserDetails();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (userDetailsListenerRegistration != null) {
            userDetailsListenerRegistration.remove();
        }
    }

    private void getUserDetails() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            if (FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber() != null) {
//                getConversations();
                userDetailsListenerRegistration = usersRef.document(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber())
                        .addSnapshotListener(userDetailsEventListener);
            }
        }
    }

    private final EventListener<DocumentSnapshot> userDetailsEventListener = (value, error) -> {
        if (value != null) {
            User user = value.toObject(User.class);
            if (user != null) {
                updateUserDetailsUI(user);
                DatabaseUtils.setCurrentUser(user);
            }
            Log.d(TAG, "onCreateView: got data");
        }
    };

    private void updateUserDetailsUI(User user) {
        binding.welcomeTv.setText(String.format("Welcome, %s", user.getName()));

        if (binding.profileImage.getDrawable() == null || checkIfUserPicIsDifferent(user, DatabaseUtils.getCurrentUser())) {
            CircularProgressDrawable circularProgressDrawable = getCircularProgressDrawable(requireContext());
            Glide.with(requireContext())
                    .load(user.getPictureUrl())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .apply(RequestOptions.circleCropTransform())
                    .placeholder(circularProgressDrawable)
                    .fallback(R.drawable.ic_baseline_person_24)
                    .transition(withCrossFade())
                    .into(binding.profileImage);
        }
    }

    private void initOnClicks() {
        binding.profileImage.setOnClickListener(view -> {
            binding.profileImage.setTransitionName("settingsTransition");

            FragmentNavigator.Extras extras = new FragmentNavigator.Extras.Builder()
                    .addSharedElement(binding.profileImage, "settingsTransition")
                    .build();

            NavDirections navDirections = HomeFragmentDirections.actionHomeFragmentToSettingsFragment();

            MaterialFadeThrough hold = new MaterialFadeThrough();
            hold.setDuration(getResources().getInteger(R.integer.transition_duration_millis));

            setExitTransition(hold);
            setReenterTransition(hold);

            Navigation.findNavController(binding.getRoot()).navigate(navDirections, extras);
        });

        binding.addTransactionBtn.setOnClickListener(view -> {
            Toast.makeText(requireContext(), "Not yet bruh", Toast.LENGTH_SHORT).show();
        });

        binding.walletDropDownBtn.setOnClickListener(view -> {
            Toast.makeText(requireContext(), "Yo calm down", Toast.LENGTH_SHORT).show();
        });

        binding.seeAllTransactionsBtn.setOnClickListener(view -> {
            Toast.makeText(requireContext(), "GET OUT RN", Toast.LENGTH_SHORT).show();
        });

        binding.adjustBallanceBtn.setOnClickListener(view -> {
            Toast.makeText(requireContext(), "Imma adjust it later sure", Toast.LENGTH_SHORT).show();
        });
    }

}