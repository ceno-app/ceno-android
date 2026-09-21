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
) : CenoViewHolder(view) {
    private val binding = HomeSectionHeaderLayoutBinding.bind(itemView)

    enum class ListState {
        HIDDEN,
        HIDDEN_HALF,
        VISIBLE
    }

    var listState: ListState = ListState.HIDDEN_HALF

    init {
        binding.tvSectionTitle.setCompoundDrawablesRelativeWithIntrinsicBounds(
            null,
            null,
            when (listState) {
                ListState.HIDDEN -> ContextCompat.getDrawable(
                    view.context,
                    R.drawable.ic_arrow_collapsed
                )

                ListState.HIDDEN_HALF -> ContextCompat.getDrawable(
                    view.context,
                    R.drawable.outline_arrows_more_down_24
                )

                ListState.VISIBLE -> ContextCompat.getDrawable(
                    view.context,
                    R.drawable.ic_arrow_expanded
                )
            },
            null
        )
        binding.tvSectionTitle.setOnClickListener {
            listState = when (listState) {
                ListState.HIDDEN -> ListState.HIDDEN_HALF
                ListState.HIDDEN_HALF -> ListState.VISIBLE
                ListState.VISIBLE -> ListState.HIDDEN
            }
            interactor.onSectionHeaderClicked(listState)
            binding.tvSectionTitle.setCompoundDrawablesRelativeWithIntrinsicBounds(
                null,
                null,
                when (listState) {
                    ListState.HIDDEN -> ContextCompat.getDrawable(
                        view.context,
                        R.drawable.ic_arrow_collapsed
                    )

                    ListState.HIDDEN_HALF -> ContextCompat.getDrawable(
                        view.context,
                        R.drawable.outline_arrows_more_down_24
                    )

                    ListState.VISIBLE -> ContextCompat.getDrawable(
                        view.context,
                        R.drawable.ic_arrow_expanded
                    )
                },
                null
            )
        }
    }

    companion object {
        val homepageCardType = HomepageCardType.SECTION_HEADER
    }
}
