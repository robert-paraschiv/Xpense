package com.rokudo.xpense.fragments;

import static androidx.recyclerview.widget.RecyclerView.VERTICAL;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.rokudo.xpense.adapters.TransactionsAdapter;
import com.rokudo.xpense.data.viewmodels.BankApiViewModel;
import com.rokudo.xpense.data.retrofit.models.BankTransaction;
import com.rokudo.xpense.databinding.FragmentAccTransListBinding;
import com.rokudo.xpense.models.Transaction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AccTransListFragment extends Fragment implements TransactionsAdapter.OnTransactionClickListener {
    private static final String TAG = "AccTransListFragment";

    private FragmentAccTransListBinding binding;
    private TransactionsAdapter adapter;
    private List<BankTransaction> bankTransactionList = new ArrayList<>();
    private List<Transaction> transactionList = new ArrayList<>();
    private BankApiViewModel bankApiViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentAccTransListBinding.inflate(inflater, container, false);

        bankApiViewModel = new ViewModelProvider(requireActivity()).get(BankApiViewModel.class);

        initOnClicks();
        buildRecyclerView();
        getArgsPassed();

        return binding.getRoot();
    }

    private void initOnClicks() {
        binding.backBtn.setOnClickListener(v ->
                Navigation.findNavController(binding.getRoot()).popBackStack());
    }

    private void buildRecyclerView() {
        adapter = new TransactionsAdapter(transactionList, false, this);
        binding.transactionsRv.setLayoutManager(new LinearLayoutManager(requireContext(), VERTICAL, false));
        binding.transactionsRv.setAdapter(adapter);
//        adapter.setOnItemClickListener();
    }

    private void getArgsPassed() {
        AccTransListFragmentArgs args = AccTransListFragmentArgs.fromBundle(requireArguments());

        bankApiViewModel.getAccountTransactions(args.getAccId())
                .observe(getViewLifecycleOwner(), transactionsResponse -> {
                    if (transactionsResponse == null || transactionsResponse.getTransactions() == null) {
                        Log.e(TAG, "onResponse: null trans response");
                    } else {

                        binding.progressIndicator.setVisibility(View.GONE);

                        bankTransactionList.addAll(Arrays.asList(transactionsResponse.getTransactions().getBooked()));
                        bankTransactionList.addAll(Arrays.asList(transactionsResponse.getTransactions().getPending()));
                        for (int i = 0; i < bankTransactionList.size(); i++) {
                            BankTransaction bankTransaction = bankTransactionList.get(i);

                            Transaction transaction = new Transaction();
                            transaction.setId(bankTransaction.getTransactionId());
                            transaction.setAmount(bankTransaction.getTransactionAmount().getAmount().doubleValue());
                            transaction.setCurrency(bankTransaction.getTransactionAmount().getCurrency());
                            transaction.setTitle(bankTransaction.getRemittanceInformationUnstructured());

                            transactionList.add(transaction);
                            adapter.notifyItemInserted(transactionList.indexOf(transaction));
                        }
                    }
                });
    }

    @Override
    public void onClick(Transaction transaction) {

    }
}