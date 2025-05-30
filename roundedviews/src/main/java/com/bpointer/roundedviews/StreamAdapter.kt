package com.bpointer.roundedviews

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView

class StreamAdapter(context: Context) : ArrayAdapter<ColorItem?>(context, 0) {
    private val mInflater: LayoutInflater = LayoutInflater.from(getContext())

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: ViewGroup
        if (convertView == null) {
            view = mInflater.inflate(R.layout.rounded_item, parent, false) as ViewGroup
        } else {
            view = convertView as ViewGroup
        }

        val item = getItem(position)

        (view.findViewById<View?>(R.id.imageView1) as ImageView).setImageResource(item!!.mResId)
        (view.findViewById<View?>(R.id.imageView1) as ImageView).setScaleType(item.mScaleType)
        (view.findViewById<View?>(R.id.textView1) as TextView).setText(item.mLine1)
        (view.findViewById<View?>(R.id.textView2) as TextView).setText(item.mLine2)
        (view.findViewById<View?>(R.id.textView3) as TextView).setText(item.mScaleType.toString())
        return view
    }
}
