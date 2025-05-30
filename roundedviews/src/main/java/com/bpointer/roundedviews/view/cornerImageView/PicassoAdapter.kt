package com.bpointer.roundedviews.view.cornerImageView

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bpointer.roundedimageview.RoundedTransformationBuilder
import com.bpointer.roundedviews.R
import com.squareup.picasso.Picasso
import com.squareup.picasso.Transformation

class PicassoAdapter(context: Context) : ArrayAdapter<PicassoItem?>(context, 0) {
    private val mInflater: LayoutInflater = LayoutInflater.from(getContext())
    private val mTransformation: Transformation? = RoundedTransformationBuilder()
        .cornerRadiusDp(30F)
        .borderColor(Color.BLACK)
        .borderWidthDp(3F)
        .oval(false)
        .build()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: ViewGroup
        if (convertView == null) {
            view = mInflater.inflate(R.layout.picasso_item, parent, false) as ViewGroup
        } else {
            view = convertView as ViewGroup
        }

        val item = getItem(position)

        val imageView = (view.findViewById<View?>(R.id.imageView1) as ImageView)
        imageView.setScaleType(item!!.mScaleType)

        Picasso.get()
            .load(item.mUrl)
            .fit()
            .transform(mTransformation!!)
            .into(imageView);


        (view.findViewById<View?>(R.id.textView3) as TextView).setText(item.mScaleType.toString())
        return view
    }
}
