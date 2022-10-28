package com.rokudo.xpense.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.rokudo.xpense.R;
import com.rokudo.xpense.models.Invitation;

import java.util.List;

public class InvitationAdapter extends RecyclerView.Adapter<InvitationAdapter.ViewHolder> {
    private final List<Invitation> invitationList;

    public InvitationAdapter(List<Invitation> invitationList) {
        this.invitationList = invitationList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final TextView personName;
        final TextView walletName;
        final ImageView personPic;
        final Chip acceptChip;
        final Chip declineChip;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            personName = itemView.findViewById(R.id.invitationPersonName);
            walletName = itemView.findViewById(R.id.walletTitle);
            personPic = itemView.findViewById(R.id.invitationPersonImage);
            acceptChip = itemView.findViewById(R.id.acceptChip);
            declineChip = itemView.findViewById(R.id.declineChip);
        }

        @Override
        public void onClick(View v) {

        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_invitation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Invitation invitation = invitationList.get(position);
        if (invitation == null) {
            return;
        }

        holder.walletName.setText(invitation.getWallet_title());
        holder.personName.setText(invitation.getCreator_name());
    }

    @Override
    public int getItemCount() {
        return invitationList.size();
    }
}
