package com.rokudo.xpense.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rokudo.xpense.R;
import com.rokudo.xpense.data.retrofit.models.Account;
import com.rokudo.xpense.data.retrofit.models.AccountDetails;
import com.rokudo.xpense.utils.dialogs.BankAccsListDialog;

import java.util.List;

public class BankAccAdapter extends RecyclerView.Adapter<BankAccAdapter.ViewHolder> {
    List<AccountDetails> accountDetailsList;

    private final BankAccsListDialog.OnBAccClickListener onBAccClickListener;

    public BankAccAdapter(List<AccountDetails> accountDetailsList, BankAccsListDialog.OnBAccClickListener onBAccClickListener) {
        this.accountDetailsList = accountDetailsList;
        this.onBAccClickListener = onBAccClickListener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView accountType, accountIban, accountCurrency;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.accountType = itemView.findViewById(R.id.bankAccType);
            this.accountIban = itemView.findViewById(R.id.bankAccIban);
            this.accountCurrency = itemView.findViewById(R.id.bankAccCurrency);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (onBAccClickListener != null) {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    onBAccClickListener.onAccountClick(position);
                }
            }
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bank_account, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AccountDetails accountDetails = accountDetailsList.get(position);
        if (accountDetails != null && accountDetails.getAccount() != null) {
            Account account = accountDetails.getAccount();
            holder.accountType.setText(account.getProduct());
            holder.accountIban.setText(account.getIban());
            holder.accountCurrency.setText(account.getCurrency());
        }
    }

    @Override
    public int getItemCount() {
        return accountDetailsList.size();
    }
}
