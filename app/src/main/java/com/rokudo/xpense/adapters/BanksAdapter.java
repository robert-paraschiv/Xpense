package com.rokudo.xpense.adapters;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

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
import com.rokudo.xpense.data.retrofit.models.Institution;
import com.rokudo.xpense.utils.dialogs.DialogUtils;

import java.util.List;

public class BanksAdapter extends RecyclerView.Adapter<BanksAdapter.ViewHolder> {
    private final List<Institution> bankList;

    private OnBankTapListener mListener;

    public BanksAdapter(List<Institution> bankList) {
        this.bankList = bankList;
    }

    public void setOnItemClickListener(OnBankTapListener listener) {
        mListener = listener;
    }

    public interface OnBankTapListener {
        void onClick(Institution institution);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final ImageView bankPic;
        final TextView bankName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.bankName = itemView.findViewById(R.id.bank_name);
            this.bankPic = itemView.findViewById(R.id.bank_picture);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mListener != null) {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    mListener.onClick(bankList.get(position));
                }
            }
        }
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bank, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Institution institution = bankList.get(position);
        if (institution != null) {
            holder.bankName.setText(institution.getName());

            Glide.with(holder.bankPic)
                    .load(institution.getLogo())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .apply(RequestOptions.circleCropTransform())
                    .placeholder(DialogUtils.getCircularProgressDrawable(holder.bankPic.getContext()))
                    .fallback(R.drawable.ic_baseline_person_24)
                    .transition(withCrossFade())
                    .into(holder.bankPic);
        }
    }

    @Override
    public int getItemCount() {
        return bankList.size();
    }

}
