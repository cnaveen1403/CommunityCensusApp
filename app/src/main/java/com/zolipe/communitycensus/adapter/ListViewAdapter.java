package com.zolipe.communitycensus.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.zolipe.communitycensus.R;
import com.zolipe.communitycensus.model.FamilyHead;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import gr.escsoft.michaelprimez.searchablespinner.interfaces.ISpinnerSelectedView;

public class ListViewAdapter extends BaseAdapter implements Filterable, ISpinnerSelectedView {
    private Context mContext;
    private List<FamilyHead> mBackupList;
    private List<FamilyHead> mList;
    private StringFilter mObjFilter = new StringFilter();
    private String LOG_TAG = "ListViewAdapter";

    public ListViewAdapter(Context context, List<FamilyHead> list) {
        mContext = context;
        mList = list;
        mBackupList = list;
    }

    @Override
    public int getCount() {
        return mList == null ? 0 : mList.size();
    }

    @Override
    public Object getItem(int position) {
        if (mList != null && position > 0)
            return mList.get(position);
        else
            return null;
    }

    @Override
    public long getItemId(int position) {
        if (mList == null && position > 0)
            return mList.get(position).hashCode();
        else
            return -1;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = null;
        /*if (position == 0) {
            view = getNoSelectionView();
        } else {*/
            view = View.inflate(mContext, R.layout.search_list_view, null);
            CircleImageView letters = (CircleImageView) view.findViewById(R.id.ImgVw_Letters);
            TextView dispalyName = (TextView) view.findViewById(R.id.TxtVw_DisplayName);

        Glide.with(mContext).load(mList.get(position).getImage_url())
//                    .thumbnail(0.5f)
                    .dontAnimate()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.ic_family_head)
                    .into(letters);
//            letters.setImageDrawable(getTextDrawable(mList.get(position-1)));
            dispalyName.setText(mList.get(position).getFirst_name() + " " + mList.get(position).getLast_name());
//        }
        return view;
    }

    @Override
    public View getSelectedView(int position) {
        View view = null;
//        if (position == 0) {
//            view = getNoSelectionView();
//        } else {
            view = View.inflate(mContext, R.layout.search_list_view, null);
            ImageView letters = (ImageView) view.findViewById(R.id.ImgVw_Letters);
            TextView dispalyName = (TextView) view.findViewById(R.id.TxtVw_DisplayName);
            Glide.with(mContext).load(mList.get(position).getImage_url())
//                    .thumbnail(0.5f)
                    .crossFade()
                    .dontAnimate()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.ic_family_head)
                    .into(letters);
//            letters.setImageDrawable(getTextDrawable(mStrings.get(position-1)));
            dispalyName.setText(mList.get(position).getFirst_name() + " " + mList.get(position).getLast_name());
//        }
        return view;
    }

    @Override
    public View getNoSelectionView() {
        View view = View.inflate(mContext, R.layout.view_list_no_selection_item, null);
        return view;
    }

    @Override
    public Filter getFilter() {
        return mObjFilter;
    }

    public class StringFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            final FilterResults filterResults = new FilterResults();
            if (TextUtils.isEmpty(constraint)) {
                filterResults.count = mBackupList.size();
                filterResults.values = mBackupList;
                return filterResults;
            }
/*            Log.d(LOG_TAG, "Here is the control");*/
//           new AddMember().callAsync(constraint.toString());
            final ArrayList<FamilyHead> filterStrings = new ArrayList<>();
            for (FamilyHead tempObj : mBackupList) {
                if (tempObj.getFirst_name().toLowerCase().contains(constraint)) {
                    filterStrings.add(tempObj);
                }
            }

            filterResults.count = filterStrings.size();
            filterResults.values = filterStrings;
            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mList = (ArrayList) results.values;
            notifyDataSetChanged();
        }
    }

    private class ItemView {
        public ImageView mImageView;
        public TextView mTextView;
    }

    public enum ItemViewType {
        ITEM, NO_SELECTION_ITEM;
    }
}
