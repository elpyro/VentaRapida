package com.example.ventarapida.ui.registros

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.example.ventarapida.ui.registros.pestanas.ListaCompras
import com.example.ventarapida.ui.registros.pestanas.FacturaVentas

class MyPagerAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager) {

    private val titles = arrayOf("Tab 1", "Tab 2", "Tab 3")

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> FacturaVentas()
            1 -> ListaCompras()
            2 -> FacturaVentas()
            else -> FacturaVentas()
        }
    }

    override fun getCount(): Int {
        return titles.size
    }

    override fun getPageTitle(position: Int): CharSequence {
        return titles[position]
    }
}