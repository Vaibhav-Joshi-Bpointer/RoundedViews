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
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.bpointer.roundedviews.R

class ExampleActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {
    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.example_activity)

        val toolbar: Toolbar? = findViewById(R.id.toolbar) as Toolbar?
        val navSpinner = findViewById(R.id.spinner_nav) as Spinner

        navSpinner.setAdapter(
            ArrayAdapter.createFromResource(
                navSpinner.getContext(),
                R.array.action_list,
                android.R.layout.simple_spinner_dropdown_item
            )
        )

        navSpinner.setOnItemSelectedListener(this)

        if (savedInstanceState == null) {
            navSpinner.setSelection(0)
        }
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val newFragment: Fragment?
        when (position) {
            0 ->         // bitmap
                newFragment = RoundedFragment.Companion.getInstance(ExampleType.DEFAULT)

            1 ->         // oval
                newFragment = RoundedFragment.Companion.getInstance(ExampleType.OVAL)

            2 ->         // select
                newFragment =
                    RoundedFragment.Companion.getInstance(ExampleType.SELECT_CORNERS)

            3 ->         // picasso
                newFragment = PicassoFragment()

            4 ->         // color
                newFragment = ColorFragment()

            5 ->         // background
                newFragment = RoundedFragment.Companion.getInstance(ExampleType.BACKGROUND)

            else ->
                newFragment = RoundedFragment.Companion.getInstance(ExampleType.DEFAULT)
        }

        getSupportFragmentManager().beginTransaction()
            .replace(R.id.fragment_container, newFragment)
            .commit()
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {}
}