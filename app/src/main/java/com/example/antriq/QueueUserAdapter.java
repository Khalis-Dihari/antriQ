// QueueUserAdapter.java
package com.example.antriq;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.FirebaseDatabase;

import java.util.*;

public class QueueUserAdapter extends RecyclerView.Adapter<QueueUserAdapter.UserViewHolder> {

    private final Context context;
    private final List<UserInQueue> userList;
    private final String queueId;

    public QueueUserAdapter(Context context, List<UserInQueue> userList, String queueId) {
        this.context = context;
        this.userList = userList;
        this.queueId = queueId;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_user_queue, parent, false);
        return new UserViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        UserInQueue user = userList.get(position);

        holder.tvName.setText(user.name);
        holder.tvNumber.setText("No: " + String.format("%03d", user.number));
        holder.tvStatus.setText("Status: " + user.status);

        // Expand/collapse panel
        holder.ivArrow.setOnClickListener(v -> {
            boolean isVisible = holder.panelDetail.getVisibility() == View.VISIBLE;
            holder.panelDetail.setVisibility(isVisible ? View.GONE : View.VISIBLE);
        });

        // Tombol ubah status
        holder.btnChangeStatus.setOnClickListener(v -> {
            holder.panelStatus.setVisibility(View.VISIBLE);
        });

        // Spinner status
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"Menunggu", "Dipanggil", "Sedang Dilayani", "Selesai", "Dibatalkan"});
        holder.spStatus.setAdapter(adapter);

        holder.btnSaveStatus.setOnClickListener(v -> {
            String selectedStatus = holder.spStatus.getSelectedItem().toString();

            FirebaseDatabase.getInstance().getReference("queues")
                    .child(queueId)
                    .child("users")
                    .child(user.userId)
                    .child("status")
                    .setValue(selectedStatus);

            holder.tvStatus.setText("Status: " + selectedStatus);
            holder.panelStatus.setVisibility(View.GONE);
        });

        holder.btnDelete.setOnClickListener(v -> {
            FirebaseDatabase.getInstance().getReference("queues")
                    .child(queueId)
                    .child("users")
                    .child(user.userId)
                    .removeValue();
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvNumber, tvStatus;
        ImageView ivArrow;
        LinearLayout panelDetail, panelStatus;
        Button btnChangeStatus, btnSaveStatus, btnDelete;
        Spinner spStatus;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvNumber = itemView.findViewById(R.id.tvNumber);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            ivArrow = itemView.findViewById(R.id.ivArrow);
            panelDetail = itemView.findViewById(R.id.panelDetail);
            panelStatus = itemView.findViewById(R.id.panelStatus);
            btnChangeStatus = itemView.findViewById(R.id.btnChangeStatus);
            btnSaveStatus = itemView.findViewById(R.id.btnSaveStatus);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            spStatus = itemView.findViewById(R.id.spStatus);
        }
    }
}
