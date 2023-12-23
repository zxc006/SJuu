package com.github.tvbox.osc.beanry;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.tvbox.osc.R;

import java.util.List;

public class NoticeAdapter extends RecyclerView.Adapter<NoticeAdapter.ViewHolder> {
    private List<NoticeBean.MsgDTO> msg;

    public NoticeAdapter(List<NoticeBean.MsgDTO> msg){
        this.msg = msg;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.dialog_gonggao,parent,false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.textView.setText(msg.get(position).content);
        holder.time.setText(msg.get(position).date);
        holder.name.setText(msg.get(position).name);
    }

    @Override
    public int getItemCount() {
        return msg.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        TextView textView;
        TextView time;
        TextView name;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.tv_container);
            time = itemView.findViewById(R.id.time);
            name = itemView.findViewById(R.id.name);
        }
    }

}
