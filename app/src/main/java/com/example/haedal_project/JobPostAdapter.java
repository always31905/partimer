package com.example.haedal_project;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class JobPostAdapter extends RecyclerView.Adapter<JobPostAdapter.VH> {
    private List<JobPost> list = new ArrayList<>();
    private OnItemClickListener listener;

    /**
     * 클릭 이벤트를 처리하기 위한 인터페이스
     */
    public interface OnItemClickListener {
        void onItemClick(JobPost post);
    }

    /**
     * 외부에서 클릭 리스너를 설정할 수 있는 메서드
     */
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    /**
     * 데이터 리스트를 설정하고 RecyclerView를 갱신합니다.
     */
    public void setItems(List<JobPost> posts) {
        this.list.clear();
        if (posts != null) {
            this.list.addAll(posts);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_job_post, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        JobPost post = list.get(position);
        holder.tvTitle.setText(post.getTitle());
        holder.tvLocation.setText(post.getLocation());
        holder.tvPay.setText(post.getPay() + "원/시간");
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    /**
     * ViewHolder 내에서 클릭 이벤트를 감지하여 전달합니다.
     */
    class VH extends RecyclerView.ViewHolder {
        TextView tvTitle, tvLocation, tvPay;

        VH(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvPay = itemView.findViewById(R.id.tvPay);

            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onItemClick(list.get(pos));
                }
            });
        }
    }
}
