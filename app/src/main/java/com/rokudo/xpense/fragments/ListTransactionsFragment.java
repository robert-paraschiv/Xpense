package com.rokudo.xpense.fragments;

import static androidx.recyclerview.widget.RecyclerView.VERTICAL;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.transition.MaterialSharedAxis;
import com.rokudo.xpense.R;
import com.rokudo.xpense.adapters.OnTransClickListener;
import com.rokudo.xpense.adapters.TransactionsAdapter;
import com.rokudo.xpense.data.viewmodels.StatisticsViewModel;
import com.rokudo.xpense.databinding.FragmentListTransactionsBinding;
import com.rokudo.xpense.models.Transaction;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ListTransactionsFragment extends Fragment implements OnTransClickListener {

    private FragmentListTransactionsBinding binding;
    private TransactionsAdapter adapter;

    boolean gotTransactionsOnce = false;
    private final List<Transaction> transactionList = new ArrayList<>();
    private String walletId;
    private String currency;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentListTransactionsBinding.inflate(inflater, container, false);

        buildRecyclerView();
        ListTransactionsFragmentArgs args = ListTransactionsFragmentArgs.fromBundle(requireArguments());
        walletId = args.getWalletId();
        currency = args.getWalletCurrency();
        loadTransactions();

        binding.backBtn.setOnClickListener(view -> Navigation.findNavController(view).popBackStack());

        return binding.getRoot();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MaterialSharedAxis enter = new MaterialSharedAxis(MaterialSharedAxis.X, true);
        enter.setDuration(getResources().getInteger(R.integer.transition_duration_millis));
        MaterialSharedAxis exit = new MaterialSharedAxis(MaterialSharedAxis.X, false);
        exit.setDuration(getResources().getInteger(R.integer.transition_duration_millis));
        setEnterTransition(enter);
        setReturnTransition(exit);
    }

    private void buildRecyclerView() {
        adapter = new TransactionsAdapter(transactionList, false, this);
        binding.transactionsRv.setLayoutManager(new LinearLayoutManager(requireContext(), VERTICAL, false));
        binding.transactionsRv.setAdapter(adapter);
//        adapter.setOnItemClickListener();
    }

    private void loadTransactions() {
        StatisticsViewModel statisticsViewModel = new ViewModelProvider(requireActivity())
                .get(StatisticsViewModel.class);

        if (statisticsViewModel.getStoredStatisticsDoc() == null) {
            return;
        }

        List<Transaction> transactions = new ArrayList<>(statisticsViewModel.getStoredStatisticsDoc().getTransactions().values());
        transactions.sort(Comparator.comparingLong(Transaction::getDateLong).reversed());

        transactions.forEach(transaction -> {
            if (transactionList.contains(transaction)) {
                transactionList.set(transactionList.indexOf(transaction), transaction);
                adapter.notifyItemChanged(transactionList.indexOf(transaction));
            } else {
                if (gotTransactionsOnce) {
                    transactionList.add(0, transaction);
                    adapter.notifyItemInserted(0);
                } else {
                    transactionList.add(transaction);
                    adapter.notifyItemInserted(transactionList.size() - 1);
                }
            }
        });
    }

    @Override
    public void onClick(Transaction transaction) {

        MaterialSharedAxis exit = new MaterialSharedAxis(MaterialSharedAxis.X, true);
        exit.setDuration(getResources().getInteger(R.integer.transition_duration_millis));
        MaterialSharedAxis reenter = new MaterialSharedAxis(MaterialSharedAxis.X, false);
        reenter.setDuration(getResources().getInteger(R.integer.transition_duration_millis));

        setExitTransition(exit);
        setReenterTransition(reenter);

        Navigation.findNavController(binding.getRoot()).navigate(
                ListTransactionsFragmentDirections
                        .actionListTransactionsFragmentToAddTransactionLayout(walletId,
                                currency,
                                transaction,
                                true));
    }
}