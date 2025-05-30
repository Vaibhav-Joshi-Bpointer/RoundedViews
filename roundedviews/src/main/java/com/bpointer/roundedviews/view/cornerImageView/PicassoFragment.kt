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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView.ScaleType
import android.widget.ListView
import androidx.fragment.app.Fragment
import com.bpointer.roundedviews.R

class PicassoFragment : Fragment() {
    public override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view: View = inflater.inflate(R.layout.fragment_rounded, container, false)

        val adapter: PicassoAdapter = PicassoAdapter(requireActivity())
        (view.findViewById<View?>(R.id.main_list) as ListView).setAdapter(adapter)

        adapter.add(
            PicassoItem(
                "http://24.media.tumblr.com/2176464a507f8a34f09d58ee7fcf105a/tumblr_mzgzd79XMY1st5lhmo1_1280.jpg",
                ScaleType.CENTER
            )
        )
        adapter.add(
            PicassoItem(
                "http://25.media.tumblr.com/af50758346e388e6e69f4c378c4f264f/tumblr_mzgzcdEDTL1st5lhmo1_1280.jpg",
                ScaleType.CENTER_CROP
            )
        )
        adapter.add(
            PicassoItem(
                "http://24.media.tumblr.com/5f97f94756bf706bf41ac0dd37b585cf/tumblr_mzgzbdYBht1st5lhmo1_1280.jpg",
                ScaleType.CENTER_INSIDE
            )
        )
        adapter.add(
            PicassoItem(
                "http://24.media.tumblr.com/6ddffd6a6036f61a1f2b1744bad77730/tumblr_mzgz9vJ1CK1st5lhmo1_1280.jpg",
                ScaleType.FIT_CENTER
            )
        )
        adapter.add(
            PicassoItem(
                "http://25.media.tumblr.com/104330dfee76bb4713ea6c424a339b31/tumblr_mzgz92BX471st5lhmo1_1280.jpg",
                ScaleType.FIT_END
            )
        )
        adapter.add(
            PicassoItem(
                "http://25.media.tumblr.com/c2aa498a075ab4b0c1b7c56120c140ab/tumblr_mzgz8arzYo1st5lhmo1_1280.jpg",
                ScaleType.FIT_START
            )
        )
        adapter.add(
            PicassoItem(
                "http://25.media.tumblr.com/e886622da66651f4818f441e3120127d/tumblr_mzgz6yFP0u1st5lhmo1_1280.jpg",
                ScaleType.FIT_XY
            )
        )

        return view
    }


}