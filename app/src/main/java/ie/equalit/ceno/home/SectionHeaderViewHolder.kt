package ie.equalit.ceno.home

import android.view.View
import androidx.core.content.ContextCompat
import ie.equalit.ceno.R
import ie.equalit.ceno.databinding.HomeSectionHeaderLayoutBinding
import ie.equalit.ceno.home.sessioncontrol.HomePageInteractor
import ie.equalit.ceno.utils.view.CenoViewHolder

class SectionHeaderViewHolder(
    view: View,
    interactor: HomePageInteractor
): CenoViewHolder(view) {
    private val binding = HomeSectionHeaderLayoutBinding.bind(itemView)

    var listIsHidden : Boolean = true

    init {
        binding.tvSectionTitle.setCompoundDrawablesRelativeWithIntrinsicBounds(
            null,
            null,
            if (listIsHidden) ContextCompat.getDrawable(view.context, R.drawable.ic_arrow_collapsed) else ContextCompat.getDrawable(view.context, R.drawable.ic_arrow_expanded),
            null
        )
        binding.tvSectionTitle.setOnClickListener {
            listIsHidden = !listIsHidden
            interactor.onSectionHeaderClicked(listIsHidden)
            binding.tvSectionTitle.setCompoundDrawablesRelativeWithIntrinsicBounds(
                null,
                null,
                if (listIsHidden) ContextCompat.getDrawable(view.context, R.drawable.ic_arrow_collapsed) else ContextCompat.getDrawable(view.context, R.drawable.ic_arrow_expanded),
                null
            )
        }
    }

    companion object {
        val homepageCardType = HomepageCardType.SECTION_HEADER
    }
}