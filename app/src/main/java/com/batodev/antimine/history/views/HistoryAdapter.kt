package com.batodev.antimine.history.views

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.batodev.antimine.R
import com.batodev.antimine.databinding.ViewHistoryItemBinding
import com.batodev.antimine.history.viewmodel.HistoryEvent
import com.google.android.material.color.MaterialColors
import dev.lucasnlm.antimine.common.level.database.models.Save
import dev.lucasnlm.antimine.common.level.database.models.SaveStatus
import dev.lucasnlm.antimine.core.models.Difficulty
import dev.lucasnlm.antimine.core.viewmodel.StatelessViewModel
import com.google.android.material.R as GR
import dev.lucasnlm.antimine.i18n.R as i18n

class HistoryAdapter(
    private val saveHistory: List<Save>,
    private val statelessViewModel: StatelessViewModel<HistoryEvent>,
) : RecyclerView.Adapter<HistoryViewHolder>() {
    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): HistoryViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return HistoryViewHolder(
            binding = ViewHistoryItemBinding.inflate(layoutInflater, parent, false),
        )
    }

    override fun getItemId(position: Int): Long {
        return saveHistory[position].uid.toLong()
    }

    override fun getItemCount(): Int {
        return saveHistory.size
    }

    private fun difficultyStringRes(difficulty: Difficulty): Int =
        when (difficulty) {
            Difficulty.Beginner -> i18n.string.beginner
            Difficulty.Intermediate -> i18n.string.intermediate
            Difficulty.Expert -> i18n.string.expert
            Difficulty.Standard -> i18n.string.standard
            Difficulty.Master -> i18n.string.master
            Difficulty.Legend -> i18n.string.legend
            Difficulty.Custom -> i18n.string.custom
            Difficulty.FixedSize -> i18n.string.fixed_size
        }

    private fun bindReplayButton(
        binding: ViewHistoryItemBinding,
        save: Save,
        buttonBackgroundColor: ColorStateList?,
    ) {
        val context = binding.root.context
        binding.replay.run {
            icon =
                ContextCompat.getDrawable(
                    context,
                    if (save.status != SaveStatus.VICTORY) R.drawable.replay else R.drawable.play,
                )
            setOnClickListener {
                statelessViewModel.sendEvent(HistoryEvent.ReplaySave(save.uid))
            }
            backgroundTintList = buttonBackgroundColor
        }
    }

    private fun bindOpenButton(
        binding: ViewHistoryItemBinding,
        save: Save,
        buttonBackgroundColor: ColorStateList?,
    ) {
        binding.open.run {
            setOnClickListener {
                statelessViewModel.sendEvent(HistoryEvent.LoadSave(save.uid))
            }
            backgroundTintList = buttonBackgroundColor
        }
    }

    override fun onBindViewHolder(
        holder: HistoryViewHolder,
        position: Int,
    ) {
        val save = saveHistory[position]
        val context = holder.itemView.context
        val buttonBackgroundColor =
            MaterialColors.getColorStateListOrNull(
                context,
                GR.attr.colorOnBackground,
            )?.withAlpha(BUTTON_BACKGROUND_ALPHA)

        val difficultyText = context.getString(difficultyStringRes(save.difficulty))
        val gameNameText = "$difficultyText #${save.uid}"

        holder.binding.run {
            difficulty.text = gameNameText
            badge.alpha = if (save.status == SaveStatus.VICTORY) BADGE_VICTORY_ALPHA else BADGE_DEFEAT_ALPHA

            minefieldSize.text =
                context.getString(i18n.string.minefield_size, save.minefield.width, save.minefield.height)
            minesCount.text = context.getString(i18n.string.mines_remaining, save.minefield.mines)
        }

        bindReplayButton(holder.binding, save, buttonBackgroundColor)
        bindOpenButton(holder.binding, save, buttonBackgroundColor)
    }

    companion object {
        const val BUTTON_BACKGROUND_ALPHA = 50
        const val BADGE_VICTORY_ALPHA = 1.0f
        const val BADGE_DEFEAT_ALPHA = 0.5f
    }
}

class HistoryViewHolder(
    val binding: ViewHistoryItemBinding,
) : RecyclerView.ViewHolder(binding.root)
