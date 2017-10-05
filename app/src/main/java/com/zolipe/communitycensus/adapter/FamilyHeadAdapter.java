package com.zolipe.communitycensus.adapter;

import android.content.Context;
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
import com.zolipe.communitycensus.interfaces.FamilyHeadsListItemClickListener;
import com.zolipe.communitycensus.model.FamilyHead;

import java.util.ArrayList;
import java.util.List;

public class FamilyHeadAdapter extends RecyclerView.Adapter<FamilyHeadAdapter.MyViewHolder> implements Filterable {

    Context context;
    public List<FamilyHead> headsList;
    public List<FamilyHead> mBackupList;
    private ItemFilter mFilter = new ItemFilter();
    private FamilyHeadsListItemClickListener itemClickListener;

    @Override
    public Filter getFilter() {
        return mFilter;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public TextView name, phone_number, tv_familySizeVal, tv_family_size;
        //        public ImageView profile_pic, icon_edit, icon_delete;
        public ImageView profile_pic, iv_status;

        public MyViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            name = (TextView) itemView.findViewById(R.id.txtTitle);
            phone_number = (TextView) itemView.findViewById(R.id.txtPhone);
            profile_pic = (ImageView) itemView.findViewById(R.id.imgIcon);
            tv_family_size = (TextView)itemView.findViewById(R.id.tv_family_size);
            tv_familySizeVal = (TextView)itemView.findViewById(R.id.tv_family_size_value);
            iv_status = (ImageView)itemView.findViewById(R.id.iv_status);
//            icon_edit = (ImageView) itemView.findViewById(R.id.ic_edit);
//            icon_delete = (ImageView) itemView.findViewById(R.id.ic_delete);
        }

        @Override
        public void onClick(View v) {
            Log.e("Adapter", "Clicked Herer *************** " + getAdapterPosition());
            if (itemClickListener != null) itemClickListener.onFamilyHeadClicked(v, headsList.get(getAdapterPosition()));
        }
    }

    public FamilyHeadAdapter(List<FamilyHead> list, Context context) {
        this.headsList = list;
        this.mBackupList = list;
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.listview_family_head_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final FamilyHeadAdapter.MyViewHolder holder, int position) {
        FamilyHead familyHeads= headsList.get(position);
        String name = familyHeads.getFirst_name() + " " + familyHeads.getLast_name();
        holder.name.setText(name);
        holder.phone_number.setText(familyHeads.getPhone_number());
        holder.tv_familySizeVal.setText(familyHeads.getFamily_size());
        int resId = (familyHeads.getIsSynced().equals("yes")?R.drawable.ic_status_green:R.drawable.ic_status_yellow);
        holder.iv_status.setImageResource(resId);
        Glide.with(context).load(familyHeads.getImage_url())
//                .thumbnail(0.5f)
                .crossFade()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.ic_family_head)
                .into(holder.profile_pic);

        /*if (position % 2 == 1) {
            holder.itemView.setBackgroundColor(Color.WHITE);
        } else {
            holder.itemView.setBackgroundColor(Color.parseColor("#03a9f5"));
            holder.name.setTextColor(Color.WHITE);
            holder.phone_number.setTextColor(Color.WHITE);
            holder.tv_family_size.setTextColor(Color.WHITE);
            holder.tv_familySizeVal.setTextColor(Color.WHITE);
        }*/
    }

    @Override
    public int getItemCount() {
        return headsList.size();
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
            final List<FamilyHead> filterStrings = new ArrayList<>();
            for (FamilyHead tempObj : mBackupList) {
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
            headsList = (ArrayList) results.values;
            notifyDataSetChanged();
        }
    }

    public void setClickListener(FamilyHeadsListItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }
}
