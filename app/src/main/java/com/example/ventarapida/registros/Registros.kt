package com.example.ventarapida.registros

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.ViewPager

import com.example.ventarapida.databinding.FragmentRegistrosBinding
import com.google.android.material.tabs.TabLayout

class Registros : Fragment() {

    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager
    private lateinit var binding: FragmentRegistrosBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentRegistrosBinding.inflate(inflater, container, false)

        // Infla el diseño antes de acceder a los elementos de UI
        val view = binding.root

        tabLayout = binding.tabLayout
        viewPager = binding.viewPager

        val adapter = MyPagerAdapter(childFragmentManager)
        viewPager.adapter = adapter

        tabLayout.setupWithViewPager(viewPager)


        return view
    }
}