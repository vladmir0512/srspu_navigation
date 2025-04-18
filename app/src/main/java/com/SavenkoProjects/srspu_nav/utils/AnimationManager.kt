package com.SavenkoProjects.srspu_nav.utils

import android.animation.Animator
import android.animation.ObjectAnimator
import android.view.View
import com.SavenkoProjects.srspu_nav.databinding.ActivityRoutesBinding

data class AnimationManager(
    private val binding: ActivityRoutesBinding
) {
    fun rotateMapAnimation(imageView: View, isFirstState: Boolean) {
        val animator: ObjectAnimator = ObjectAnimator.ofFloat(
            imageView,
            "rotationX",
            0f, 180f
        )

        animator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {
                if (isFirstState) {
                    binding.floorMapImageView.animate()
                        .alpha(0f)
                        .setDuration(300)
                        .withEndAction {
                            binding.floorMapImageView.visibility = View.GONE
                        }
                } else {
                    binding.mapImageView.animate()
                        .alpha(0f)
                        .setDuration(300)
                        .withEndAction {
                            binding.mapImageView.visibility = View.GONE
                        }
                }
            }

            override fun onAnimationEnd(animation: Animator) {
                if (!isFirstState) {
                    binding.floorMapImageView.visibility = View.VISIBLE
                    binding.floorMapImageView.animate().alpha(1f).setDuration(300).start()
                } else {
                    binding.mapImageView.visibility = View.VISIBLE
                    binding.mapImageView.animate().alpha(1f).setDuration(300).start()
                }
            }

            override fun onAnimationCancel(animation: Animator) {
                if (isFirstState) {
                    binding.floorMapImageView.animate()
                        .alpha(0f)
                        .setDuration(300)
                        .withEndAction {
                            binding.floorMapImageView.visibility = View.GONE
                        }
                } else {
                    binding.mapImageView.animate()
                        .alpha(0f)
                        .setDuration(300)
                        .withEndAction {
                            binding.mapImageView.visibility = View.GONE
                        }
                }
            }

            override fun onAnimationRepeat(animation: Animator) {}
        })

        animator.setDuration(1500)
        animator.start()
    }

    fun toggleSearchFieldAnimation(isSearchVisible: Boolean) {
        if (isSearchVisible) {
            binding.searchEditText.animate()
                .alpha(0f)
                .setDuration(300)
                .withEndAction {
                    binding.searchEditText.visibility = View.GONE
                }
        } else {
            binding.searchEditText.visibility = View.VISIBLE
            binding.searchEditText.animate().alpha(1f).setDuration(300).start()
        }
    }
} 