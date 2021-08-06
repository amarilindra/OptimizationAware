package dev.doubledot.doki.views

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import dev.doubledot.doki.R
import dev.doubledot.doki.api.extensions.DONT_KILL_MY_APP_DEFAULT_MANUFACTURER
import dev.doubledot.doki.api.models.DokiManufacturer
import dev.doubledot.doki.api.tasks.DokiApi
import dev.doubledot.doki.api.tasks.DokiApiCallback
import dev.doubledot.doki.extensions.*
import dev.doubledot.doki.models.Device

@Suppress("MemberVisibilityCanBePrivate")
class DokiContentView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : LinearLayout(context, attrs, defStyle) {

    private val api: DokiApi by lazy { DokiApi() }

    // View bindings ---------------------------------------

    private val appBarLayout : View? by bind(R.id.appbar)
    private val footerLayout : View? by bind(R.id.footer)

    private val headerBackground : View? by bind(R.id.headerBackground)

    private val deviceManufacturerHeader: TextView? by bind(R.id.deviceManufacturerHeader)
    private val deviceManufacturer: TextView? by bind(R.id.deviceManufacturer)

    private val deviceModelHeader: TextView? by bind(R.id.deviceModelHeader)
    private val deviceModel: TextView? by bind(R.id.deviceModel)

    private val deviceAndroidVersionHeader: TextView? by bind(R.id.deviceAndroidVersionHeader)
    private val deviceAndroidVersion: TextView? by bind(R.id.deviceAndroidVersion)

    private val manufacturerRatingHeader: TextView? by bind(R.id.manufacturerRatingHeader)
    private val manufacturerRating: DokiRatingView? by bind(R.id.manufacturerRating)

    private val contentLoadingView: ProgressBar? by bind(R.id.contentLoadingView)
    private val contentScrollView : View? by bind(R.id.contentScrollView)

    private val contentExplanationHeader : TextView? by bind(R.id.contentExplanationHeader)
    private val contentExplanation : DokiHtmlTextView? by bind(R.id.contentExplanation)

    private val contentSolutionHeader : TextView? by bind(R.id.contentSolutionHeader)
    private val contentSolution : DokiHtmlTextView? by bind(R.id.contentSolution)

    private val contentDeveloperSolutionHeader : TextView? by bind(R.id.contentDeveloperSolutionHeader)
    private val contentDeveloperSolution : DokiHtmlTextView? by bind(R.id.contentDeveloperSolution)

    private val contentAttribution : DokiHtmlTextView? by bind(R.id.contentAttribution)

    private val buttonContainer: View? by bind(R.id.buttonContainer)
    private val closeBtn: TextView? by bind(R.id.buttonClose)

    private val divider1: View? by bind(R.id.divider1)
    private val divider2: View? by bind(R.id.divider2)
    private val divider3: View? by bind(R.id.divider3)

    // Theming attrs ---------------------------------------

    var primaryTextColor: Int = Color.BLACK
        set(value) {
            deviceManufacturer?.setTextColor(value)
            deviceModel?.setTextColor(value)
            deviceAndroidVersion?.setTextColor(value)

            contentExplanationHeader?.setTextColor(value)
            contentSolutionHeader?.setTextColor(value)
            contentDeveloperSolutionHeader?.setTextColor(value)

            field = value
        }

    var secondaryTextColor: Int = Color.BLACK
        set(value) {
            deviceManufacturerHeader?.setTextColor(value)
            deviceModelHeader?.setTextColor(value)
            deviceAndroidVersionHeader?.setTextColor(value)
            manufacturerRatingHeader?.setTextColor(value)

            contentExplanation?.setTextColor(value)
            contentSolution?.setTextColor(value)
            contentDeveloperSolution?.setTextColor(value)
            contentAttribution?.setTextColor(value)

            field = value
        }

    var buttonsTextColor: Int = context.extractColor(intArrayOf(R.attr.colorAccent))
        set(value) {
            closeBtn?.setTextColor(value)
            contentExplanation?.linkHighlightColor = value
            contentSolution?.linkHighlightColor = value
            contentDeveloperSolution?.linkHighlightColor = value
            contentAttribution?.linkHighlightColor = value
            field = value
        }

    var dividerColor: Int = Color.parseColor("#1F000000")
        set(value) {
            divider1?.setBackgroundColor(value)
            divider2?.setBackgroundColor(value)
            divider3?.setBackgroundColor(value)
            field = value
        }

    override fun setBackgroundColor(color: Int) {
        super.setBackgroundColor(color)
        rootBackgroundColor = color
    }

    private var rootBackgroundColor: Int = Color.TRANSPARENT
        set(value) {
            appBarLayout?.setBackgroundColor(value)
            footerLayout?.setBackgroundColor(value)
            field = value
        }

    var headerBackgroundColor: Int = Color.TRANSPARENT
        set(value) {
            headerBackground?.setBackgroundColor(value)
            field = value
        }

    var activeIconsDrawable: Drawable? = null
        set(value) {
            manufacturerRating?.activeIconsDrawable = value
            field = value
        }

    var inactiveIconsDrawable: Drawable? = null
        set(value) {
            manufacturerRating?.inactiveIconsDrawable = value
            field = value
        }

    var iconsStyle : DokiRatingView.Style? = DokiRatingView.Style.THUMB
        set(value) {
            value?.let {
                activeIconsDrawable = try {
                    ContextCompat.getDrawable(context, it.activeResId)
                } catch (e: Exception) {
                    null
                }

                inactiveIconsDrawable = try {
                    ContextCompat.getDrawable(context, it.inactiveResId)
                } catch (e: Exception) {
                    null
                }
            }

            field = value
        }

    var activeIconsColor: Int = Color.BLACK
        set(value) {
            manufacturerRating?.activeIconsColor = value
            field = value
        }

    var inactiveIconsColor: Int = Color.BLACK
        set(value) {
            manufacturerRating?.inactiveIconsColor = value
            field = value
        }

    var lineHeight : Float = 1F
        set(value) {
            contentExplanation?.setLineSpacing(lineSeparation, value)
            contentSolution?.setLineSpacing(lineSeparation, value)
            contentDeveloperSolution?.setLineSpacing(lineSeparation, value)
            field = value
        }

    var lineSeparation : Float = 0F
        set(value) {
            field = value
            lineHeight = lineHeight
        }

    // Data / models ------------------------------------

    private var devSolutionMessage: String = ""
    var explanationTitleText: String = ""
    var solutionTitleText: String = ""
    var appName: String = ""

    var device : Device? = null
        set(value) {
            field = value
            value ?: return

            deviceManufacturer?.text = value.manufacturer
            deviceModel?.text = value.model
            deviceAndroidVersion?.text = value.androidVersion
        }

    var manufacturer : DokiManufacturer? = null
        set(value) {
            field = value
            value ?: return

            manufacturerRating?.rating = value.award
            manufacturerRating?.visibleIf(value.award > 0)
            manufacturerRatingHeader?.visibleIf(value.award > 0)

            contentExplanation?.htmlText = value.explanation
            contentSolution?.htmlText = value.user_solution.let {
                if(appName.isNotEmpty())
                    it.replace("your app", appName, true)
                else
                    it
            }

            if (value.dev_solution.isNullOrEmpty()) {
                contentDeveloperSolutionHeader?.visibility = GONE
                contentDeveloperSolution?.visibility = GONE
            } else {
                contentDeveloperSolutionHeader?.visibility = VISIBLE
                contentDeveloperSolution?.visibility = VISIBLE
                contentDeveloperSolution?.htmlText = value.dev_solution
            }

            contentLoadingView?.gone()
            contentScrollView?.visible()
        }

    init {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.doki_view_content, this, true)
        rootBackgroundColor = Color.WHITE

        contentAttribution?.htmlText = context.getString(R.string.doki_content_attribution)

        initFromAttrs(attrs)
        device = Device()
    }

    @SuppressLint("Recycle")
    private fun initFromAttrs(attrs: AttributeSet?) {
        val styledAttrs: TypedArray? = try {
            context.obtainStyledAttributes(attrs, R.styleable.DokiContentView, 0, 0)
        } catch (e: Exception) {
            null
        }

        // Styling

        primaryTextColor = styledAttrs?.getColorOrNull(R.styleable.DokiContentView_dokiPrimaryTextColor) ?: primaryTextColor
        secondaryTextColor = styledAttrs?.getColorOrNull(R.styleable.DokiContentView_dokiSecondaryTextColor) ?: secondaryTextColor
        buttonsTextColor = styledAttrs?.getColorOrNull(R.styleable.DokiContentView_dokiButtonsTextColor) ?: buttonsTextColor
        dividerColor = styledAttrs?.getColorOrNull(R.styleable.DokiContentView_dokiDividerColor) ?: dividerColor
        headerBackgroundColor = styledAttrs?.getColorOrNull(R.styleable.DokiContentView_dokiHeaderBackgroundColor) ?: headerBackgroundColor
        activeIconsDrawable = styledAttrs?.getDrawableOrNull(R.styleable.DokiContentView_dokiActiveIconsDrawable) ?: activeIconsDrawable
        inactiveIconsDrawable = styledAttrs?.getDrawableOrNull(R.styleable.DokiContentView_dokiInactiveIconsDrawable) ?: inactiveIconsDrawable
        iconsStyle = getStyledIconsStyle(styledAttrs) ?: iconsStyle
        activeIconsColor = styledAttrs?.getColorOrNull(R.styleable.DokiContentView_dokiActiveIconsColor) ?: activeIconsColor
        inactiveIconsColor = styledAttrs?.getColorOrNull(R.styleable.DokiContentView_dokiInactiveIconsColor) ?: inactiveIconsColor
        lineHeight = 1F
        lineSeparation = 8F.dpToPx

        // Data

        val defaultExplanationTitleText = try {
            context.getString(R.string.doki_explanation)
        } catch (e: Exception) {
            ""
        }
        explanationTitleText = try {
            styledAttrs?.getString(R.styleable.DokiContentView_dokiExplanationTitle) ?: defaultExplanationTitleText
        } catch (e: Exception) {
            defaultExplanationTitleText
        }

        val defaultSolutionTitleText = try {
            context.getString(R.string.doki_solution)
        } catch (e: Exception) {
            ""
        }
        solutionTitleText = try {
            styledAttrs?.getString(R.styleable.DokiContentView_dokiSolutionTitle) ?: defaultSolutionTitleText
        } catch (e: Exception) {
            defaultSolutionTitleText
        }

        val bgColor: Int = try {
            styledAttrs?.getColor(R.styleable.DokiContentView_dokiBackgroundColor, 0) ?: 0
        } catch (e: Exception) {
            0
        }
        if (bgColor != 0)
            setBackgroundColor(bgColor)

        styledAttrs?.recycle()
    }

    fun loadContent(
        manufacturerId: String = DONT_KILL_MY_APP_DEFAULT_MANUFACTURER,
        appName: String = ""
    ) : DokiApi {
        this.appName = appName
        api.callback = object: DokiApiCallback {
            override fun onSuccess(response: DokiManufacturer?) {
                manufacturer = response
            }
        }
        api.getManufacturer(manufacturerId)

        return api
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        api.cancel()
    }

    fun setOnCloseListener(listener: (view: View?) -> Unit = { _ -> }) {
        closeBtn?.setOnClickListener { listener(it) }
    }

    fun setButtonsVisibility(visible: Boolean) {
        buttonContainer?.visibleIf(visible)
        divider2?.visibleIf(visible)
    }

    private fun getStyledIconsStyle(attrs : TypedArray?) : DokiRatingView.Style? {
        val iconsStyleId = try {
            attrs?.getInt(R.styleable.DokiContentView_dokiIconsStyle, -1) ?: -1
        } catch (e: Exception) {
            -1
        }

        return if (iconsStyleId >= 0)
            DokiRatingView.Style.getFromId(iconsStyleId)
        else null
    }
}
