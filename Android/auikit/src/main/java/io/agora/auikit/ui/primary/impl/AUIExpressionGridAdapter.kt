package io.agora.auikit.ui.primary.impl

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.imageview.ShapeableImageView
import io.agora.auikit.R
import io.agora.auikit.model.AUIExpressionIcon

class AUIExpressionGridAdapter(
    context: Context,
    resource: Int,
    objects: MutableList<AUIExpressionIcon>
) : ArrayAdapter<AUIExpressionIcon>(context, resource, objects) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.voice_widget_row_expression, null)
        }
        val imageView = convertView!!.findViewById<ShapeableImageView>(R.id.iv_expression)
        val emojIcon: AUIExpressionIcon? = getItem(position)
        emojIcon.let {
            if (emojIcon?.icon != 0) {
                emojIcon?.icon?.let { imageView.setImageResource(it) }
            } else if (emojIcon?.iconPath != null) {
                Glide.with(context).load(emojIcon.iconPath)
                    .apply(RequestOptions.placeholderOf(R.drawable.voice_icon_default_expression))
                    .into(imageView)
            }
            return convertView
        }
    }

}