package com.dicoding.todoapp.ui.detail

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.dicoding.todoapp.R
import com.dicoding.todoapp.databinding.ActivityTaskDetailBinding
import com.dicoding.todoapp.ui.ViewModelFactory
import com.dicoding.todoapp.utils.DateConverter
import com.dicoding.todoapp.utils.TASK_ID

class DetailTaskActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTaskDetailBinding
    private lateinit var viewModel: DetailTaskViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaskDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //TODO 11 : Show detail task and implement delete action
        setupModel()
    }

    private fun setupModel() {
        viewModel = ViewModelProvider(
            this,
            ViewModelFactory.getInstance(this)
        )[DetailTaskViewModel::class.java]

        viewModel.setTaskId(intent.getIntExtra(TASK_ID, 0))

        viewModel.task.observe(this) { task ->
            if (task != null) {
                binding.detailEdTitle.setText(task.title)
                binding.detailEdDescription.setText(task.description)
                binding.detailEdDueDate.setText(DateConverter.convertMillisToString(task.dueDateMillis))
                binding.btnDeleteTask.setOnClickListener {
                    viewModel.deleteTask()
                    onBackPressed()
                }
            }
        }
    }
}