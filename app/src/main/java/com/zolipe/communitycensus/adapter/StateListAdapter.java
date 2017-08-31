package com.zolipe.communitycensus.adapter;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.zolipe.communitycensus.R;
import com.zolipe.communitycensus.model.State;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import gr.escsoft.michaelprimez.searchablespinner.interfaces.ISpinnerSelectedView;
import gr.escsoft.michaelprimez.searchablespinner.tools.UITools;

public class StateListAdapter extends BaseAdapter implements ISpinnerSelectedView {
    private Context mContext;
    private List<State> mList;

    public StateListAdapter(Context context, List<State> list) {
        mContext = context;
        mList = list;
    }

    @Override
    public int getCount() {
        return mList == null ? 0 : mList.size();
    }

    @Override
    public State getItem(int position) {
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
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        View view = null;
        if (position == -1) {
            view = getNoSelectionView();
        } else {
            view = View.inflate(mContext, R.layout.state_list_item, null);
            CircleImageView letters = (CircleImageView) view.findViewById(R.id.ImgVw_Letters);
            TextView dispalyName = (TextView) view.findViewById(R.id.TxtVw_DisplayName);
            letters.setImageDrawable(getTextDrawable(mList.get(position).getName()));
            dispalyName.setText(mList.get(position).getName());
        }
        return view;
    }

    @Override
    public View getSelectedView(int position) {
        View view = null;
        if (position == -1) {
            view = getNoSelectionView();
        } else {
            view = View.inflate(mContext, R.layout.view_list_item, null);
            ImageView letters = (ImageView) view.findViewById(R.id.ImgVw_Letters);
            TextView dispalyName = (TextView) view.findViewById(R.id.TxtVw_DisplayName);
            letters.setImageDrawable(getTextDrawable(mList.get(position).getName()));
            dispalyName.setText(mList.get(position).getName());
        }
        return view;
    }

    @Override
    public View getNoSelectionView() {
        View view = View.inflate(mContext, R.layout.view_list_no_selection_item, null);
        return view;
    }

    private TextDrawable getTextDrawable(String displayName) {
        TextDrawable drawable = null;
        if (!TextUtils.isEmpty(displayName)) {
            int color2 = ColorGenerator.MATERIAL.getColor(displayName);
            drawable = TextDrawable.builder()
                    .beginConfig()
                    .width(UITools.dpToPx(mContext, 32))
                    .height(UITools.dpToPx(mContext, 32))
                    .textColor(Color.WHITE)
                    .toUpperCase()
                    .endConfig()
                    .round()
                    .build(displayName.substring(0, 1), color2);
        } else {
            drawable = TextDrawable.builder()
                    .beginConfig()
                    .width(UITools.dpToPx(mContext, 32))
                    .height(UITools.dpToPx(mContext, 32))
                    .endConfig()
                    .round()
                    .build("?", Color.GRAY);
        }
        return drawable;
    }
}
