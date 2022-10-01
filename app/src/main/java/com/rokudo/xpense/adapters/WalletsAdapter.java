package com.rokudo.xpense.adapters;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;
import static com.rokudo.xpense.models.WalletUser.getOtherUserProfilePic;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.rokudo.xpense.R;
import com.rokudo.xpense.models.Wallet;
import com.rokudo.xpense.utils.dialogs.DialogUtils;

import java.util.List;

public class WalletsAdapter extends RecyclerView.Adapter<WalletsAdapter.ViewHolder> {
    List<Wallet> walletList;

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
        Glide.with(holder.walletItemPersonImage)
                .load(getOtherUserProfilePic(wallet.getWalletUsers()))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .apply(RequestOptions.circleCropTransform())
                .placeholder(DialogUtils.getCircularProgressDrawable(holder.walletItemPersonImage.getContext()))
                .fallback(R.drawable.ic_baseline_person_24)
                .error(R.drawable.ic_baseline_person_24)
                .transition(withCrossFade())
                .into(holder.walletItemPersonImage);

    }

    @Override
    public int getItemCount() {
        return walletList.size();
    }
}
