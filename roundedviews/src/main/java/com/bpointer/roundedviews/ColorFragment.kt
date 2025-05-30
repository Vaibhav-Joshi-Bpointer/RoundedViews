package com.bpointer.roundedviews

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.ImageView.ScaleType
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment


class ColorFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view: View = inflater.inflate(R.layout.fragment_rounded, container, false)

        val adapter: StreamAdapter = StreamAdapter(requireActivity())
        (view.findViewById<View?>(R.id.main_list) as ListView).setAdapter(adapter)

        adapter.add(
            ColorItem(
                android.R.color.darker_gray, "Tufa at night", "Mono Lake, CA",
                ScaleType.CENTER
            )
        )
        adapter.add(
            ColorItem(
                android.R.color.holo_orange_dark, "Starry night", "Lake Powell, AZ",
                ScaleType.CENTER_CROP
            )
        )
        adapter.add(
            ColorItem(
                android.R.color.holo_blue_dark, "Racetrack playa", "Death Valley, CA",
                ScaleType.CENTER_INSIDE
            )
        )
        adapter.add(
            ColorItem(
                android.R.color.holo_green_dark, "Napali coast", "Kauai, HI",
                ScaleType.FIT_CENTER
            )
        )
        adapter.add(
            ColorItem(
                android.R.color.holo_red_dark, "Delicate Arch", "Arches, UT",
                ScaleType.FIT_END
            )
        )
        adapter.add(
            ColorItem(
                android.R.color.holo_purple, "Sierra sunset", "Lone Pine, CA",
                ScaleType.FIT_START
            )
        )
        adapter.add(
            ColorItem(
                android.R.color.white, "Majestic", "Grand Teton, WY",
                ScaleType.FIT_XY
            )
        )

        return view
    }

}