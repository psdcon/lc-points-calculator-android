package com.iamapaulling.paul.leavingcertcalculator;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * Created by Paul on 03/01/2016.
 */
public class CustomSpinnerAdapter extends ArrayAdapter {

    private String[] array;
    private Context context;


    public CustomSpinnerAdapter(Context context, int resource, String[] array) {
        super(context, resource, array);

        this.array = array;
        this.context = context;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    @Override
    public View getView(int pos, View convertView, ViewGroup parent) {
        return getCustomView(pos, convertView, parent);
    }

    public View getCustomView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        View mySpinner = inflater.inflate(R.layout.custom_spinner_item, parent, false);

        View rowLayout = mySpinner.findViewById(R.id.row_layout);
        TextView textView = (TextView) mySpinner.findViewById(R.id.main_text);

        textView.setText(array[position]);
        // Make "group"
        if(array[position].equals("Languages") ||
                array[position].equals("Laboratory sciences") ||
                array[position].equals("Business Studies") ||
                array[position].equals("Applied sciences") ||
                array[position].equals("Arts and Humanities") ||
                array[position].equals("Non-curricular languages")){

            textView.setTextSize(14);
            textView.setTextColor(Color.parseColor("#8A000000"));
            textView.setAllCaps(true);
            textView.setPadding(4, textView.getPaddingTop(), textView.getPaddingRight(), textView.getPaddingBottom());
            rowLayout.setBackgroundResource(R.drawable.border_top);
        }

        return mySpinner;
    }

    @Override
    public boolean isEnabled(int position) {
//        return super.isEnabled(position);
        if(array[position].equals("Languages") ||
                array[position].equals("Laboratory sciences") ||
                array[position].equals("Business Studies") ||
                array[position].equals("Applied sciences") ||
                array[position].equals("Arts and Humanities") ||
                array[position].equals("Non-curricular languages")){
            return false;
        }
        return true;
    }
}
