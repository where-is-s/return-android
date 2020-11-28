package re.returnapp.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Annotation
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StrikethroughSpan
import android.view.View
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_entity.*
import re.returnapp.*
import re.returnapp.Collection
import re.returnapp.dialog.ChooseTypeDialog
import re.returnapp.dialog.FindEntityDialog
import re.returnapp.model.Entity
import re.returnapp.model.Type


class EntityActivity : AppCompatActivity() {

    private var id: Int = 0
    private var type: Type = Type.PEOPLE

    companion object {
        private const val EXTRA_TYPE = "Type"
        private const val EXTRA_ID = "Id"

        fun start(context: Context, id: Int) {
            val intent = Intent(context, EntityActivity::class.java).apply {
                putExtra(EXTRA_ID, id)
            }
            context.startActivity(intent)
        }

        fun start(context: Context, type: Type) {
            val intent = Intent(context, EntityActivity::class.java).apply {
                putExtra(EXTRA_TYPE, type.toString())
            }
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_entity)
        id = intent.extras?.getInt(EXTRA_ID, -1) ?: -1
        type = Type.valueOf(intent.extras?.getString(EXTRA_TYPE) ?: Type.PEOPLE.toString())
        val entity = Collection.getEntityById(id) ?: Entity(Collection.newId, type)
        typeView.setImageResource(type.toDrawable())
        nameEdit.setText(entity.name)
        descriptionEdit.setTextWithMentions(entity.description)
        reviewedCheck.isChecked = entity.reviewed
        reviewedCheck.visibility = if (entity.reviewable) View.VISIBLE else View.GONE
        nameEdit.visibility = if (entity.hasNameField) View.VISIBLE else View.GONE

        mentionButton.setOnClickListener{
            val mentionDialog = FindEntityDialog(this@EntityActivity)
            mentionDialog.selectListener = object : FindEntityDialog.SelectListener {
                override fun onItemSelected(entity: Entity) {
                    val builder = SpannableStringBuilder(descriptionEdit.text)
                    val place = descriptionEdit.selectionEnd
                    val nameStr = '\uFEFF' + entity.name + '\uFEFF'
                    builder.insert(place, nameStr)
                    builder.setSpan(Annotation("entityId", entity.id.toString()), place, place + nameStr.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    if (entity.reviewed) {
                        builder.setSpan(
                            StrikethroughSpan(),
                            place,
                            place + nameStr.length,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }
                    descriptionEdit.text = builder
                    descriptionEdit.moveCursor(place + nameStr.length, place + nameStr.length)
                    descriptionEdit.requestFocus()
                }
            }
            mentionDialog.show()
        }

        if (isCreating()) {
            typeView.setOnClickListener {
                val dialog = ChooseTypeDialog(this)
                dialog.selectListener = object : ChooseTypeDialog.SelectListener {
                    override fun onItemSelected(type: Type) {
                        this@EntityActivity.type = type
                        typeView.setImageResource(type.toDrawable())
                    }
                }
                dialog.show()
            }
        }

        (if (entity.hasNameField && entity.name.isEmpty()) nameEdit else descriptionEdit).requestFocus()

        typeView.importance = entity.importance
        importanceSeekBar.progress = entity.importance
        importanceSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                typeView.importance = progress
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
    }

    private fun isCreating(): Boolean {
        return id == -1
    }

    override fun onPause() {
        val entity = Collection.getEntityById(id) ?: Entity(Collection.newId, type)
        val newName = nameEdit.text.toString()
        val newDescription = descriptionEdit.getTextWithMentions()
        val newImportance = importanceSeekBar.progress
        if (entity.name != newName || entity.description != newDescription || entity.reviewed != reviewedCheck.isChecked || entity.importance != newImportance) {
            entity.name = newName
            entity.description = newDescription
            entity.updated = System.currentTimeMillis()
            entity.reviewed = reviewedCheck.isChecked
            entity.importance = newImportance
            if (isCreating()) {
                Collection.addEntity(entity)
                id = entity.id
            } else {
                Collection.applyChanges()
            }
            Collection.save()
        }
        super.onPause()
    }

}