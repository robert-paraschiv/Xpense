package com.rokudo.xpense.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rokudo.xpense.R;
import com.rokudo.xpense.models.Wallet;

import java.util.ArrayList;
import java.util.List;

public class WalletsAdapter extends RecyclerView.Adapter<WalletsAdapter.ViewHolder> {
    List<Wallet> walletList = new ArrayList<>();

    public WalletsAdapter(List<Wallet> walletList) {
        this.walletList = walletList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView walletItemTitle;
        final TextView walletItemAmount;
        final ImageView walletItemPersonImage;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            walletItemTitle = itemView.findViewById(R.id.walletItemTitle);
            walletItemAmount = itemView.findViewById(R.id.walletItemAmount);
            walletItemPersonImage = itemView.findViewById(R.id.walletItemPersonImage);
        }
    }

    @NonNull
    @Override
    public WalletsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_wallet, parent, false);
        return new WalletsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WalletsAdapter.ViewHolder holder, int position) {
        Wallet wallet = walletList.get(position);
        holder.walletItemTitle.setText(wallet.getTitle());
        holder.walletItemAmount.setText(String.format("%s %s", wallet.getCurrency(), wallet.getAmount().toString()));

    }

    @Override
    public int getItemCount() {
        return walletList.size();
    }
}
