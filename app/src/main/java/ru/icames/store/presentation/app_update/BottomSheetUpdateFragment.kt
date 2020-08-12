package ru.icames.store.presentation.app_update

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.bottom_sheet_update.*
import kotlinx.android.synthetic.main.bottom_sheet_update.view.*
import ru.icames.store.BuildConfig
import ru.icames.store.R
import ru.icames.store.application.App
import ru.icames.store.domain.model.AppUpdateInfo
import ru.icames.store.util.DownloadManager
import ru.icames.store.util.FormatCommons
import javax.inject.Inject


class BottomSheetUpdateFragment : BottomSheetDialogFragment(), DownloadManager.DownloadListener {

    private val downloadManager = DownloadManager(this)

    @Inject
    lateinit var appUpdateManager: AppUpdateManager

    private val isDownloading = MutableLiveData<Boolean>().apply {
        this.value = false
    }

    init {
        App.getAppComponent().inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.setOnShowListener { dialog ->
            val d = dialog as BottomSheetDialog
            val bottomSheet = d.findViewById<View>(R.id.design_bottom_sheet) as FrameLayout
            val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.bottom_sheet_update, container, false)
        val appUpdateInfo = appUpdateManager.getUpdateInfo()
        if(appUpdateInfo != null) {
            view.textViewCurrentVersionName.text = "Текущая версия: ${BuildConfig.VERSION_NAME}"
            view.textViewAvailableVersionName.text = "Доступная версия: ${appUpdateInfo.versionName}"
            view.textViewUpdateSize.text = "Размер обновления: ${FormatCommons.formatFileSize(appUpdateInfo.updateFile.file.fileSize.toLong())}"
            view.textViewCreationDate.text = "Дата выхода: ${appUpdateInfo.creationDate}"
        }

        view.buttonUpdate.setOnClickListener {
            if(appUpdateInfo != null) {
                downloadManager.download(appUpdateInfo)
            }
        }

        view.buttonLater.setOnClickListener {
            dismiss()
        }

        view.buttonCancel.setOnClickListener {
            downloadManager.cancel()
        }
        observe()
        return view
    }

    override fun onDestroy() {
        super.onDestroy()
        appUpdateManager.updateShowTime()
    }

    private fun observe(){
        DownloadManager.percent.observe(this, Observer {
            progressBarDownloading.progress = it.toInt()
        })

        DownloadManager.downloadProgress.observe(this, Observer {
            textViewDownloadingProgress.text = it
        })

        isDownloading.observe(this, Observer {
            if(it) {
                layoutUpdateInfo.visibility = View.GONE
                layoutDownload.visibility = View.VISIBLE
                isCancelable = false
                textViewTitle.text = getString(R.string.label_update_downloading)
            } else {
                layoutUpdateInfo.visibility = View.VISIBLE
                layoutDownload.visibility = View.GONE
                isCancelable = true
                textViewTitle.text = getString(R.string.label_update_available)
            }
        })
    }

    override fun onStartDownload() {
        isDownloading.postValue(true)
        App.log("onStartDownload")
    }

    override fun onFileDownloaded(appUpdateInfo: AppUpdateInfo, path: String) {
        App.log("onFileDownloaded")
        appUpdateManager.startUpdateApp(path)
        isDownloading.postValue(false)
        dismiss()
    }

    override fun onCancelDownload() {
        isDownloading.postValue(false)
        dismiss()
    }

    override fun onErrorDownload() {
        isDownloading.postValue(false)
        App.log("onErrorDownload")
    }

}