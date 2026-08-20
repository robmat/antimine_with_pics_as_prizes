package com.batodev.antimine.l10n.views

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.batodev.antimine.databinding.ViewLocalizationItemBinding
import com.batodev.antimine.l10n.models.GameLanguage
import java.util.Locale

class LocalizationItemAdapter(
    private val gameLanguages: List<GameLanguage>,
    private val onSelectLanguage: (Locale) -> Unit,
) : RecyclerView.Adapter<LocalizationItemViewHolder>() {
    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): LocalizationItemViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return LocalizationItemViewHolder(
            binding = ViewLocalizationItemBinding.inflate(layoutInflater, parent, false),
        )
    }

    override fun getItemViewType(position: Int): Int = 0

    override fun getItemId(position: Int): Long = gameLanguages[position].id.toLong()

    override fun getItemCount(): Int = gameLanguages.size

    override fun onBindViewHolder(
        holder: LocalizationItemViewHolder,
        position: Int,
    ) {
        holder.binding.language.apply {
            text = gameLanguages[position].name
            setOnClickListener {
                onSelectLanguage(gameLanguages[position].locale)
            }
        }
    }
}

class LocalizationItemViewHolder(
    val binding: ViewLocalizationItemBinding,
) : RecyclerView.ViewHolder(binding.root)
