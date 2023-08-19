package com.example.tracknews

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.tracknews.databinding.FragmentToolbarBinding


class ToolbarFragment : Fragment() {

    private lateinit var binding: FragmentToolbarBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentToolbarBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.fragToolbarButtonMenu.setOnClickListener {
            Log.d("TAG1", "Toolbar Button Click")
            Toast.makeText(context, "Hello", Toast.LENGTH_SHORT).show()
        }
    }
    companion object {

        @JvmStatic
        fun newInstance() = ToolbarFragment()
    }
}