package com.rokudo.xpense.fragments;

import static androidx.recyclerview.widget.RecyclerView.VERTICAL;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rokudo.xpense.adapters.TransactionsAdapter;
import com.rokudo.xpense.data.retrofit.GetDataService;
import com.rokudo.xpense.data.retrofit.RetrofitClientInstance;
import com.rokudo.xpense.data.retrofit.models.BankTransaction;
import com.rokudo.xpense.data.retrofit.models.TransactionsResponse;
import com.rokudo.xpense.databinding.FragmentAccTransListBinding;
import com.rokudo.xpense.models.Transaction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AccTransListFragment extends Fragment implements TransactionsAdapter.OnTransactionClickListener {
    private static final String TAG = "AccTransListFragment";

    private FragmentAccTransListBinding binding;
    private TransactionsAdapter adapter;
    private List<BankTransaction> bankTransactionList = new ArrayList<>();
    private List<Transaction> transactionList = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentAccTransListBinding.inflate(inflater, container, false);

        buildRecyclerView();
        getArgsPassed();

        return binding.getRoot();
    }

    private void buildRecyclerView() {
        adapter = new TransactionsAdapter(transactionList, false, this);
        binding.transactionsRv.setLayoutManager(new LinearLayoutManager(requireContext(), VERTICAL, false));
        binding.transactionsRv.setAdapter(adapter);
//        adapter.setOnItemClickListener();
    }

    private void getArgsPassed() {
        AccTransListFragmentArgs args = AccTransListFragmentArgs.fromBundle(requireArguments());

        GetDataService service = RetrofitClientInstance.geInstance().create(GetDataService.class);
        Call<TransactionsResponse> call = service.getAccountTransactions(args.getAccId());
        call.enqueue(new Callback<TransactionsResponse>() {
            @Override
            public void onResponse(@NonNull Call<TransactionsResponse> call, @NonNull Response<TransactionsResponse> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "onResponse: " + response.body());
                    if (response.body() != null) {
                        bankTransactionList.addAll(Arrays.asList(response.body().getTransactions().getBooked()));
                        bankTransactionList.addAll(Arrays.asList(response.body().getTransactions().getPending()));
                    }
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
                } else {
                    Log.e(TAG, "onResponse: " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<TransactionsResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "onResponse: " + t.getMessage());
            }
        });
    }

    @Override
    public void onClick(Transaction transaction) {

    }
}