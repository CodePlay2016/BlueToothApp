package com.example.thomashuu.bluetoothapp;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import java.util.List;
import java.util.Map;

/**
 * Created by Thomas Huu on 2016/11/29.
 */

public class ListAdapter extends BaseAdapter {
    private List<Map<String, Object>> list;
    private LayoutInflater inflater;

    public ListAdapter(Context context) {
        inflater = LayoutInflater.from(context);
    }

    public void setList(List<Map<String, Object>> list) {
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int i) {
        return list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        ViewHolder holder;

        if(convertView == null) {
            convertView = inflater.inflate(R.layout.item, null);
            holder = new ViewHolder();

            holder.name = (TextView) convertView.findViewById(R.id.name);
            holder.address = (TextView) convertView.findViewById(R.id.address);
            convertView.setTag(holder);
        }//important
        else{
            holder = (ViewHolder) convertView.getTag();
        }

        Map map = list.get(position);
        holder.name.setText((String) map.get("name"));
        holder.address.setText((String) map.get("address"));

        Button btn = (Button) convertView.findViewById(R.id.cn_button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("hfq", "点击");
            }
        });

        return convertView;
    }

    private class ViewHolder{
//        ImageView logo;
//        TextView title;
        TextView name;
        TextView address;

    }
}

