package com.github.encryptor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class CustomAdapter extends ArrayAdapter<ListItems> {
    private final Context context;
    private final ArrayList<ListItems> itemsList;

    public CustomAdapter(Context context, ArrayList<ListItems> itemsList) {
        super(context, R.layout.list_view_item, itemsList);
        this.context = context;
        this.itemsList = itemsList;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = null;
        if (convertView != null){
            view = convertView;
        }else {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.list_view_item, parent, false);
        }
        TextView title = view.findViewById(R.id.text);
        title.setText(itemsList.get(position).getTitle());
        ImageView image = view.findViewById(R.id.image);
        if (itemsList.get(position).getIsImage()) {
            image.setImageBitmap(itemsList.get(position).getImage());
        }else
            image.setImageResource(itemsList.get(position).getIcon());
        CheckBox checkBox = view.findViewById(R.id.checkbox);
        checkBox.setTag(position);
        checkBox.setOnCheckedChangeListener(null);
        checkBox.setChecked(itemsList.get(position).getChecked());
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                itemsList.get((int) buttonView.getTag()).setChecked(isChecked);
                ((MainActivity) context).enableTrash();
            }
        });
        if (itemsList.get(position).isFile()) {
            TextView size = view.findViewById(R.id.filesize);
            String str = itemsList.get(position).getSize() + " bytes";
            size.setText(str);
        }
        return view;
    }
}
