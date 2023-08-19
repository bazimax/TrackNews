package com.example.tracknews

//import android.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.tracknews.databinding.FragmentMainBinding

import androidx.fragment.app.activityViewModels


class MainFragment : Fragment() {

    lateinit var binding: FragmentMainBinding
    private val vm: ViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentMainBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        //проверка на поворот экрана. Работает от обратного.
        if (binding.frameLayoutMainFragmentLand != null) {
            vm.statusLandscape.value = "false"
        }
        else vm.statusLandscape.value = "true"

    }

    companion object {
        @JvmStatic
        fun newInstance() = MainFragment()
    }
}