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
import com.rokudo.xpense.utils.dialogs.WalletListDialog;

import java.util.List;

public class WalletsAdapter extends RecyclerView.Adapter<WalletsAdapter.ViewHolder> {
    private final List<Wallet> walletList;
    private final WalletListDialog.OnClickListener onItemClickListener;

    public WalletsAdapter(List<Wallet> walletList, WalletListDialog.OnClickListener onClickListener) {
        this.walletList = walletList;
        this.onItemClickListener = onClickListener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final TextView walletItemTitle;
        final TextView walletItemAmount;
        final ImageView walletItemPersonImage;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            walletItemTitle = itemView.findViewById(R.id.walletItemTitle);
            walletItemAmount = itemView.findViewById(R.id.walletItemAmount);
            walletItemPersonImage = itemView.findViewById(R.id.walletItemPersonImage);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (onItemClickListener != null) {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    onItemClickListener.onWalletClick(walletList.get(position));
                }
            }
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

        if (wallet.getWalletUsers() == null || wallet.getWalletUsers().isEmpty() || wallet.getWalletUsers().size() < 2) {
            holder.walletItemPersonImage.setVisibility(View.GONE);
        } else {
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

    }

    @Override
    public int getItemCount() {
        return walletList.size();
    }
}
