package com.batodev.antimine.stats

import android.os.Bundle
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.batodev.antimine.R
import com.batodev.antimine.databinding.ActivityStatsBinding
import com.batodev.antimine.stats.view.StatsAdapter
import com.batodev.antimine.stats.viewmodel.StatsEvent
import com.batodev.antimine.stats.viewmodel.StatsViewModel
import dev.lucasnlm.antimine.ui.ext.ThemedActivity
import dev.lucasnlm.antimine.ui.model.TopBarAction
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import dev.lucasnlm.antimine.i18n.R as i18n

class StatsActivity : ThemedActivity() {
    private val statsViewModel by viewModel<StatsViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityStatsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.stats.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
        }

        lifecycleScope.launch {
            statsViewModel.sendEvent(StatsEvent.LoadStats)

            statsViewModel.observeState().collect {
                if (it.stats.isNotEmpty()) {
                    setTopBarAction(
                        TopBarAction(
                            name = i18n.string.delete_all,
                            icon = R.drawable.delete,
                            action = { confirmAndDelete() },
                        ),
                    )
                }

                binding.stats.adapter = StatsAdapter(it.stats)
                binding.empty.isVisible = it.stats.isEmpty()
            }
        }

        bindToolbar(binding.toolbar)
    }

    private fun confirmAndDelete() {
        MaterialAlertDialogBuilder(this)
            .setTitle(i18n.string.are_you_sure)
            .setMessage(i18n.string.delete_all_message)
            .setNegativeButton(i18n.string.cancel, null)
            .setPositiveButton(i18n.string.delete_all) { _, _ ->
                lifecycleScope.launch {
                    statsViewModel.sendEvent(StatsEvent.DeleteStats)
                }
                setTopBarAction(null)
            }
            .show()
    }
}
