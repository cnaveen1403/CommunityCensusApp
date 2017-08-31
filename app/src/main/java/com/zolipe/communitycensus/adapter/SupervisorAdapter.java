package com.zolipe.communitycensus.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.zolipe.communitycensus.R;
import com.zolipe.communitycensus.interfaces.SupervisorListItemClickListener;
import com.zolipe.communitycensus.model.SupervisorObj;

import java.util.ArrayList;
import java.util.List;

public class SupervisorAdapter extends RecyclerView.Adapter<SupervisorAdapter.MyViewHolder> implements Filterable {

    Context context;
    public List<SupervisorObj> supervisorList;
    public List<SupervisorObj> mBackupList;
    private ItemFilter mFilter = new ItemFilter();
    private SupervisorListItemClickListener itemClickListener;

    @Override
    public Filter getFilter() {
        return mFilter;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public TextView name, phone_number, tv_members_size, tv_family_size;
//        public ImageView profile_pic, icon_edit, icon_delete;
        public ImageView profile_pic;

        public MyViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            name = (TextView) itemView.findViewById(R.id.txtTitle);
            phone_number = (TextView) itemView.findViewById(R.id.txtPhone);
            profile_pic = (ImageView) itemView.findViewById(R.id.imgIcon);
            tv_members_size = (TextView) itemView.findViewById(R.id.tv_members_size_value);
            tv_family_size = (TextView) itemView.findViewById(R.id.tv_family_size);
//            icon_edit = (ImageView) itemView.findViewById(R.id.ic_edit);
//            icon_delete = (ImageView) itemView.findViewById(R.id.ic_delete);
        }

        @Override
        public void onClick(View v) {
            Log.d("Adapter", "Clicked Herer *************** ");
            if (itemClickListener != null) itemClickListener.onClick(v, supervisorList.get(getAdapterPosition()));
        }
    }

    public SupervisorAdapter(List<SupervisorObj> list, Context context) {
        this.supervisorList = list;
        this.mBackupList = list;
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.listview_supervisor_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        SupervisorObj supervisor = supervisorList.get(position);
        String name = supervisor.getFirst_name() + " " + supervisor.getLast_name();
        holder.name.setText(name);
        holder.phone_number.setText(supervisor.getPhone_number());
        holder.tv_members_size.setText(supervisor.getMember_count());
        holder.profile_pic.setImageResource(R.drawable.app_icon);
        Glide.with(context).load(supervisor.getImage_url())
//                .thumbnail(0.5f)
                .crossFade()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.ic_supervisor_list)
                .into(holder.profile_pic);

        /*if (position % 2 == 1) {
            holder.itemView.setBackgroundColor(Color.WHITE);
        } else {
            holder.itemView.setBackgroundColor(Color.parseColor("#03a9f5"));
            holder.name.setTextColor(Color.WHITE);
            holder.phone_number.setTextColor(Color.WHITE);
            holder.tv_members_size.setTextColor(Color.WHITE);
            holder.tv_family_size.setTextColor(Color.WHITE);
        }*/
    }

    @Override
    public int getItemCount() {
        return supervisorList.size();
    }

    private class ItemFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            final FilterResults filterResults = new FilterResults();
            if (TextUtils.isEmpty(constraint)) {
                filterResults.count = mBackupList.size();
                filterResults.values = mBackupList;
                return filterResults;
            }
/*            Log.d(LOG_TAG, "Here is the control");*/
            final List<SupervisorObj> filterStrings = new ArrayList<>();
            for (SupervisorObj tempObj : mBackupList) {
                if (tempObj.getFirst_name().toLowerCase().contains(constraint.toString().toLowerCase())) {
                    filterStrings.add(tempObj);
                }
            }

            filterResults.count = filterStrings.size();
            filterResults.values = filterStrings;
            return filterResults;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            supervisorList = (ArrayList) results.values;
            notifyDataSetChanged();
        }
    }

    public void setClickListener(SupervisorListItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }
}
