package com.example.antriq;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class QueueAdapter extends RecyclerView.Adapter<QueueAdapter.QueueViewHolder> {

    public interface OnQueueClickListener {
        void onQueueClick(Queue queue);
    }

    private List<Queue> queueList;
    private OnQueueClickListener listener;

    public QueueAdapter(List<Queue> queueList, OnQueueClickListener listener) {
        this.queueList = queueList;
        this.listener = listener;
    }

    public void setQueueList(List<Queue> queueList) {
        this.queueList = queueList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public QueueViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_queue, parent, false);
        return new QueueViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QueueViewHolder holder, int position) {
        Queue queue = queueList.get(position);
        holder.tvQueueName.setText(queue.name);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onQueueClick(queue);
            }
        });
    }

    @Override
    public int getItemCount() {
        return queueList != null ? queueList.size() : 0;
    }

    public static class QueueViewHolder extends RecyclerView.ViewHolder {
        TextView tvQueueName;

        public QueueViewHolder(@NonNull View itemView) {
            super(itemView);
            tvQueueName = itemView.findViewById(R.id.tvQueueName);
        }
    }
}
