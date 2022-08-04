package com.example.tracknews.TestFragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LifecycleOwner
import com.example.tracknews.ViewModel
import com.example.tracknews.databinding.FragmentTest1Binding

class TestFragment1 : Fragment() {

    lateinit var binding: FragmentTest1Binding

    var nState = 0

    private val vm: ViewModel by activityViewModels()

    /*//сохранение состояния
    val vm: DataModel.SavedStateViewModel by viewModels()
    //val vm: DataModel.SavedStateViewModel by activityViewModels()*/
    //val mainDbManager = MainDbManager(this)


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentTest1Binding.inflate(inflater)
        return binding.root
        //return inflater.inflate(R.layout.fragment_test1, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        nState = savedInstanceState?.getInt("counter", 0) ?: 1
        /*if (savedInstanceState == null) {
            nState = 1;
        } else {
            nState = savedInstanceState.getInt("counter", 0);
        }*/
        //retainInstance = true
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.fragTest1EditTextDate.setText(nState.toString())
        binding.fragTest1EditTextTitle.setText(vm.nState.toString())
        binding.fragTest1EditTextLink.setText("${vm.counterB}")
        vm.messagePortrait.observe(activity as LifecycleOwner) {
            binding.fragTest1EditTextList.text = it
        }
        binding.fragTest1buttonSave.setOnClickListener {
            nState++
            vm.nState++
            vm.counterB++

            binding.fragTest1EditTextDate.setText("fragment = $nState")
            binding.fragTest1EditTextTitle.setText("dataModel = ${vm.nState}")
            vm.messagePortrait.value = "true + $nState"
            binding.fragTest1EditTextLink.setText("${vm.counterB}")

            //saveToDb()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("counter", nState)
    }

    companion object {

        @JvmStatic
        fun newInstance() = TestFragment1()
    }
    /*fun saveToDb(){
        Log.d("TAG1", "TestFragment 1 -> button")
        binding.fragTest1EditTextList.text = ""
        mainDbManager.openDb()
        mainDbManager.insertToDb(
            binding.fragTest1EditTextDate.toString(),
            binding.fragTest1EditTextTitle.toString(),
            binding.fragTest1EditTextContent.toString(),
            binding.fragTest1EditTextLink.toString())
        val dataList = mainDbManager.readDbData()
        for (item in dataList){
            binding.fragTest1EditTextList.append(item)
            binding.fragTest1EditTextList.append("\n")
        }
    }*/
}