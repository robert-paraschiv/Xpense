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
import com.rokudo.xpense.models.User;
import com.rokudo.xpense.utils.dialogs.DialogUtils;

import java.util.List;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ViewHolder> {

    private final List<User> contactList;
    private OnContactClickListener mListener;

    public ContactsAdapter(List<User> contactList) {
        this.contactList = contactList;
    }

    public void setOnItemClickListener(OnContactClickListener listener) {
        mListener = listener;
    }

    public interface OnContactClickListener {
        void onClick(User user);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final ImageView picture;
        final TextView name, phoneNumber;

        public ViewHolder(final View itemView) {
            super(itemView);
            this.picture = itemView.findViewById(R.id.contact_profilePicture);
            this.name = itemView.findViewById(R.id.contact_name);
            this.phoneNumber = itemView.findViewById(R.id.contact_phoneNumber);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mListener != null) {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    mListener.onClick(contactList.get(position));
                }
            }
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = contactList.get(position);
        if (user != null) {
            holder.name.setText(user.getName());
            holder.phoneNumber.setText(user.getPhoneNumber());

            Glide.with(holder.picture)
                    .load(user.getPictureUrl())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .apply(RequestOptions.circleCropTransform())
                    .placeholder(DialogUtils.getCircularProgressDrawable(holder.picture.getContext()))
                    .fallback(R.drawable.ic_baseline_person_24)
                    .transition(withCrossFade())
                    .into(holder.picture);
        }
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }

}
