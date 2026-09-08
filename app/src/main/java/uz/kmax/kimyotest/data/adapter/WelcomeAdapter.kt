package uz.kmax.kimyotest.data.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import uz.kmax.kimyotest.R

class WelcomeAdapter(val ctx: Context) : RecyclerView.Adapter<WelcomeAdapter.ViewHolder>() {
    private val images = intArrayOf(
        R.raw.hello_animation1,
        R.raw.lottie_animation_science,
        R.raw.lottie_animation_element,
        R.raw.test_collection3,
        R.raw.checking_iq5
    )

    private val titles = intArrayOf(
        R.string.welcome_1,
        R.string.welcome_2,
        R.string.welcome_3,
        R.string.welcome_4,
        R.string.welcome_5
    )

    private val descriptions = intArrayOf(
        R.string.welcome_1_desc,
        R.string.welcome_2_desc,
        R.string.welcome_3_desc,
        R.string.welcome_4_desc,
        R.string.welcome_5_desc
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(ctx).inflate(R.layout.item_welcome, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.animation.setAnimation(images[position])
        holder.title.setText(titles[position])
        holder.description.setText(descriptions[position])
    }

    override fun getItemCount(): Int {
        return images.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var animation: LottieAnimationView = itemView.findViewById(R.id.lottieAnimation)
        var title: android.widget.TextView = itemView.findViewById(R.id.welcomeTitle)
        var description: android.widget.TextView = itemView.findViewById(R.id.welcomeDesc)
    }
}