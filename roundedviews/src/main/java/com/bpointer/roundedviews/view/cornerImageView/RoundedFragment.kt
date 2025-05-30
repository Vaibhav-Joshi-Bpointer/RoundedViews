/*
* Copyright (C) 2014 Vincent Mi
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package com.bpointer.roundedviews.view.cornerImageView

import android.content.Context
import android.graphics.Shader.TileMode
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView.ScaleType
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bpointer.roundedimageview.RoundedImageView
import com.bpointer.roundedviews.R

class RoundedFragment : Fragment() {
    private var exampleType: ExampleType? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let{
            exampleType = ExampleType.valueOf(it.getString(ARG_EXAMPLE_TYPE).toString())
        }
/*        if (getArguments() != null) {

            exampleType = ExampleType.valueOf(getArguments().getString(ARG_EXAMPLE_TYPE))
        }*/
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view: View = inflater.inflate(R.layout.fragment_rounded, container, false)

        val adapter = StreamAdapter(requireActivity())

        adapter.add(
            StreamItem(
                requireActivity(),
                R.drawable.photo1, "Tufa at night", "Mono Lake, CA", ScaleType.CENTER
            )
        )
        adapter.add(
            StreamItem(
                requireActivity(),
                R.drawable.photo2, "Starry night", "Lake Powell, AZ", ScaleType.CENTER_CROP
            )
        )
        adapter.add(
            StreamItem(
                requireActivity(),
                R.drawable.photo3, "Racetrack playa", "Death Valley, CA", ScaleType.CENTER_INSIDE
            )
        )
        adapter.add(
            StreamItem(
                requireActivity(),
                R.drawable.photo4, "Napali coast", "Kauai, HI", ScaleType.FIT_CENTER
            )
        )
        adapter.add(
            StreamItem(
                requireActivity(),
                R.drawable.photo5, "Delicate Arch", "Arches, UT", ScaleType.FIT_END
            )
        )
        adapter.add(
            StreamItem(
                requireActivity(),
                R.drawable.photo6, "Sierra sunset", "Lone Pine, CA", ScaleType.FIT_START
            )
        )
        adapter.add(
            StreamItem(
                requireActivity(),
                R.drawable.photo7, "Majestic", "Grand Teton, WY", ScaleType.FIT_XY
            )
        )
        adapter.add(
            StreamItem(
                requireActivity(),
                R.drawable.black_white_tile, "TileMode", "REPEAT", ScaleType.FIT_XY,
                TileMode.REPEAT
            )
        )
        adapter.add(
            StreamItem(
                requireActivity(),
                R.drawable.black_white_tile, "TileMode", "CLAMP", ScaleType.FIT_XY,
                TileMode.CLAMP
            )
        )
        adapter.add(
            StreamItem(
                requireActivity(),
                R.drawable.black_white_tile, "TileMode", "MIRROR", ScaleType.FIT_XY,
                TileMode.MIRROR
            )
        )

        (view.findViewById<View?>(R.id.main_list) as ListView).setAdapter(adapter)
        return view
    }

    internal inner class StreamAdapter(context: Context) : ArrayAdapter<StreamItem?>(context, 0) {
        private val mInflater: LayoutInflater

        init {
            mInflater = LayoutInflater.from(getContext())
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view: ViewGroup
            if (convertView == null) {
                if (exampleType == ExampleType.SELECT_CORNERS) {
                    view =
                        mInflater.inflate(R.layout.rounded_item_select, parent, false) as ViewGroup
                } else if (exampleType == ExampleType.BACKGROUND) {
                    view = mInflater.inflate(
                        R.layout.rounded_background_item,
                        parent,
                        false
                    ) as ViewGroup
                } else {
                    view = mInflater.inflate(R.layout.rounded_item, parent, false) as ViewGroup
                }
            } else {
                view = convertView as ViewGroup
            }

            val item = getItem(position)

            val iv: RoundedImageView =
                (view.findViewById<View?>(R.id.imageView1) as RoundedImageView)
            iv.setOval(exampleType == ExampleType.OVAL)
            iv.setImageBitmap(item!!.mBitmap)
            iv.setScaleType(item.mScaleType)
            iv.setTileModeX(item.mTileMode)
            iv.setTileModeY(item.mTileMode)

            (view.findViewById<View?>(R.id.textView1) as TextView).setText(item.mLine1)
            (view.findViewById<View?>(R.id.textView2) as TextView).setText(item.mLine2)
            (view.findViewById<View?>(R.id.textView3) as TextView).setText(item.mScaleType.toString())

            return view
        }
    }

    companion object {
        const val ARG_EXAMPLE_TYPE: String = "example_type"

        fun getInstance(exampleType: ExampleType): RoundedFragment {
            val f = RoundedFragment()
            val args = Bundle()
            args.putString(ARG_EXAMPLE_TYPE, exampleType.name)
            f.setArguments(args)
            return f
        }
    }
}