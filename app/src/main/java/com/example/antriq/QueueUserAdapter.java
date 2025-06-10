package com.example.antriq;

import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.*;

public class QueueUserAdapter extends RecyclerView.Adapter<QueueUserAdapter.UserVH> {

    public interface OnUserClickListener {
        void onUpdateStatus(UserInQueue user, String newStatus);
        void onDelete(UserInQueue user);
    }

    private List<UserInQueue> data;
    private final OnUserClickListener cb;
    private int expandedPos = RecyclerView.NO_POSITION;

    public QueueUserAdapter(List<UserInQueue> data, OnUserClickListener cb) {
        this.data = data;
        this.cb = cb;
    }

    public void setUserList(List<UserInQueue> newData) {
        data = newData;
        notifyDataSetChanged();
    }

    @NonNull @Override
    public UserVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user_queue, parent, false);
        return new UserVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull UserVH h, int pos) {
        UserInQueue u = data.get(pos);
        h.tvName.setText(u.name);
        h.tvNumber.setText(String.format("No: %03d", u.number));
        h.tvStatus.setText("Status: " + u.status);

        boolean expanded = pos == expandedPos;
        h.panelDetail.setVisibility(expanded ? View.VISIBLE : View.GONE);
        h.ivArrow.setRotation(expanded ? 180 : 0);

        /* ==== klik item (expand / collapse) ==== */
        h.itemView.setOnClickListener(v -> {
            int old = expandedPos;
            expandedPos = (expanded ? RecyclerView.NO_POSITION : pos);
            notifyItemChanged(old);
            notifyItemChanged(expandedPos);
        });

        /* ==== tombol ubah status ==== */
        h.btnChangeStatus.setOnClickListener(v -> {
            h.panelStatus.setVisibility(
                    h.panelStatus.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
        });

        /* ==== spinner status ==== */
        ArrayAdapter<String> spAdapter = new ArrayAdapter<>(h.itemView.getContext(),
                android.R.layout.simple_spinner_item,
                new String[]{"Menunggu", "Dipanggil", "Sedang Dilayani", "Selesai", "Dibatalkan"});
        spAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        h.spStatus.setAdapter(spAdapter);
        h.spStatus.setSelection(getStatusIndex(u.status));

        /* ==== simpan status ==== */
        h.btnSaveStatus.setOnClickListener(v -> {
            String newStatus = h.spStatus.getSelectedItem().toString();
            cb.onUpdateStatus(u, newStatus);
            h.panelStatus.setVisibility(View.GONE);
        });

        /* ==== hapus ==== */
        h.btnDelete.setOnClickListener(v -> cb.onDelete(u));
    }

    @Override public int getItemCount() { return data.size(); }

    /* ------------ ViewHolder ----------- */
    static class UserVH extends RecyclerView.ViewHolder {
        TextView tvName, tvNumber, tvStatus;
        ImageView ivArrow;
        View panelDetail, panelStatus;
        Button btnChangeStatus, btnDelete, btnSaveStatus;
        Spinner spStatus;
        UserVH(View v) {
            super(v);
            tvName = v.findViewById(R.id.tvName);
            tvNumber = v.findViewById(R.id.tvNumber);
            tvStatus = v.findViewById(R.id.tvStatus);
            ivArrow = v.findViewById(R.id.ivArrow);
            panelDetail = v.findViewById(R.id.panelDetail);
            panelStatus = v.findViewById(R.id.panelStatus);
            btnChangeStatus = v.findViewById(R.id.btnChangeStatus);
            btnDelete = v.findViewById(R.id.btnDelete);
            btnSaveStatus = v.findViewById(R.id.btnSaveStatus);
            spStatus = v.findViewById(R.id.spStatus);
        }
    }

    private int getStatusIndex(String s) {
        switch (s) {
            case "Dipanggil":        return 1;
            case "Sedang Dilayani":   return 2;
            case "Selesai":           return 3;
            case "Dibatalkan":        return 4;
            default:                  return 0;
        }
    }
}
