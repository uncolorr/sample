package ru.icames.store.presentation.code_reader

import android.annotation.SuppressLint
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Point
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.view.ContextMenu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.MultiProcessor
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.activity_qr_code_reader.*
import kotlinx.android.synthetic.main.item_founded_data.*
import kotlinx.android.synthetic.main.layout_error_reader.*
import kotlinx.android.synthetic.main.layout_readed_items.*
import kotlinx.android.synthetic.main.view_camera_sample.*
import ru.icames.store.presentation.base.UniversalAdapter
import ru.icames.store.R
import ru.icames.store.annotations.LaunchActivityResult
import ru.icames.store.application.App
import ru.icames.store.application.AppSettings
import ru.icames.store.domain.model.Code
import ru.icames.store.presentation.bar_code.BarcodeGraphic
import ru.icames.store.presentation.bar_code.BarcodeGraphicTracker
import ru.icames.store.presentation.bar_code.BarcodeTrackerFactory
import ru.icames.store.presentation.bar_code.GraphicOverlay
import ru.icames.store.presentation.code_reader.code_handler.CodeHandler
import ru.icames.store.presentation.code_reader.code_handler.StepHandler
import ru.icames.store.presentation.code_reader.config.BaseQrCodeReaderConfig
import ru.icames.store.presentation.main.MainActivity
import ru.icames.store.receiver.ScanReceiverSettings
import ru.icames.store.util.*
import ru.icames.terminal.domain.model.Step
import java.io.IOException
import kotlin.concurrent.thread

class QrCodeReaderActivity  : AppCompatActivity(),
    BarcodeGraphicTracker.BarcodeUpdateListener,
    CodeHandler.CodeHandlerListener,
    ClipboardManager.OnPrimaryClipChangedListener {

    companion object {
        private const val ARG_QR_CODE_READER_CONFIG = "qrCodeReaderConfig"

        private const val DELAY: Long = 2500

        fun getInstance(context: Context,
                        config: BaseQrCodeReaderConfig
        ): Intent {
            val intent = Intent(context, QrCodeReaderActivity::class.java)
            intent.putExtra(ARG_QR_CODE_READER_CONFIG, config)
            return intent
        }
    }

    private lateinit var loading: AlertDialog

    private lateinit var viewModel: QrCodeReaderViewModel

    private lateinit var config: BaseQrCodeReaderConfig

    private lateinit var codeHandler: CodeHandler

    private lateinit var universalAdapter: UniversalAdapter<Code>

    private lateinit var scanReceiver: BroadcastReceiver

    private lateinit var sheetBehavior: BottomSheetBehavior<LinearLayout>

    private var cameraSource: CameraSource? = null
    private var graphicOverlay: GraphicOverlay<BarcodeGraphic>? = null
    private var barcodeDetector: BarcodeDetector? = null

    private var canReadBarcode: Boolean = true

    private lateinit var readDelay: CountDownTimer

    private lateinit var showErrorDelay: CountDownTimer

    private lateinit var showFoundedDataDelay: CountDownTimer

    private lateinit var clipboardManager: ClipboardManager

    private lateinit var deviceType: DeviceType


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_code_reader)
        sheetBehavior = BottomSheetBehavior.from(bottomSheet)
        viewModel = ViewModelProviders.of(this).get(QrCodeReaderViewModel::class.java)
        loading = LoadingDialog.newInstanceWithoutCancelable(this)
        config = intent.getSerializableExtra(ARG_QR_CODE_READER_CONFIG) as BaseQrCodeReaderConfig
        deviceType = AppSettings.get<DeviceType>(AppSettings.KEY_DEVICE_TYPE)
            ?: DeviceType.SMART_LITE
        setupFoundedDataView()
        setupCodeReader()
        observeViewModel()
        buttonFinishRead.setOnClickListener {
           if (universalAdapter.isEmpty()) {
                onError("Для отправки данных требуется отсканировать хотя бы одно значение")
                return@setOnClickListener
            }
            onAllDataCollected()
        }

        buttonShowReaded.setOnClickListener {
            sheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }

        buttonBack.setOnClickListener {
            onBackPressed()
        }

        registerForContextMenu(buttonScanerMenu)
        buttonScanerMenu.setOnClickListener {
            openContextMenu(buttonScanerMenu)
        }

        buttonRepeat.setOnClickListener {
            onAllDataCollected()
        }


        /**
         * Delay for next scan reading.
         * Also it needed only for read from camera for comfortable work.
         * */
        readDelay = object : CountDownTimer(DELAY, DELAY) {
            override fun onFinish() {
                canReadBarcode = true
            }

            override fun onTick(p0: Long) {}
        }

        /**
         * Delay for showing error label.
         * When timer finish error has been hide.
         * */
        showErrorDelay = object : CountDownTimer(DELAY, DELAY) {
            override fun onFinish() {
                textViewError.visibility = View.INVISIBLE
            }

            override fun onTick(p0: Long) {}
        }

        /**
         * Delay for showing founded data card.
         * When timer finish card with data has been hide.
         * */
        showFoundedDataDelay = object : CountDownTimer(DELAY, DELAY) {
            override fun onFinish() {
                layoutFoundedData.visibility = View.INVISIBLE
            }
            override fun onTick(p0: Long) {}
        }
    }


    /**
     * Setup receiver of data depending on the Scanner model
     * It is needed because ATOL Smart Droid is a old terminal model and can read scan data only from clipboard.
     * @see setupClipboardReceiver
     * ATOL Smart Lite receive scan data from broadcast
     * @see setupBroadcastReceiver
     *
     * */
    private fun setupScanReceiver() {
        when (deviceType) {
            DeviceType.SMART_LITE -> {
                setupBroadcastReceiver()
            }

            DeviceType.SMART_DROID -> {
                setupClipboardReceiver()
            }
            DeviceType.SMARTPHONE -> {
                checkCamera()
            }
        }
    }

    private fun setupClipboardReceiver() {
        clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboardManager.addPrimaryClipChangedListener(this)

    }

    /**
     * Catch data when clipboard data changed
     * @see setupClipboardReceiver
     * */
    override fun onPrimaryClipChanged() {
        val clipData = clipboardManager.primaryClip
        if (clipData != null && clipData.itemCount > 0) {
            val value = clipData.getItemAt(0).coerceToText(App.getContext())
            App.log("Catch clipData: $value")
            handleCodeValue(value as String)
            return
        }
        handleCodeValue("")
    }

    private fun setupBroadcastReceiver() {
        scanReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                App.log("catch in qr code activity")
                if (canReadBarcode) {
                    val b = intent?.extras
                    val value = b?.getString(ScanReceiverSettings.EXTRA_SCAN_DATA_NAME) ?: ""
                    App.log("Catch broadcast data: $value")
                    handleCodeValue(value)
                }
            }
        }
        registerReceiver(scanReceiver, IntentFilter(ScanReceiverSettings.SCAN_BROADCAST_NAME))
    }

    override fun onResume() {
        super.onResume()
        setupScanReceiver()
    }

    override fun onStop() {
        super.onStop()
        when (deviceType) {
            DeviceType.SMART_LITE -> {
                unregisterReceiver(scanReceiver)
            }
            DeviceType.SMART_DROID -> {
                clipboardManager.removePrimaryClipChangedListener(this)
            }
            DeviceType.SMARTPHONE -> {
                stopCamera()
            }
        }
    }

    override fun onBackPressed() {
        if (sheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
            sheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            return
        }
        super.onBackPressed()
    }


    /**
     * Setup RecyclerView for collecting founded data
     * */

    private fun setupFoundedDataView() {
        val layoutId: Int = if (config.canDeleteItems) {
            R.layout.item_founded_data_swiped
        } else {
            R.layout.item_founded_data
        }
        universalAdapter = UniversalAdapter(layoutId)
        //universalAdapter.deleteItemListener = this
        recyclerViewFounded.apply {
            adapter = universalAdapter
            layoutManager = LinearLayoutManager(this@QrCodeReaderActivity, LinearLayoutManager.VERTICAL, false)
        }
    }

    /**
     * Setup code handler that handle scan data, prepare UI of scaner
     * and setup barcode reader from camera if is it possible
     *
     * @see setupCodeHandler
     * @see setupCodeReader
     *
     * */
    private fun setupCodeReader() {
        setupCodeHandler()
        graphicOverlay = findViewById(R.id.graphicOverlay)
        barcodeDetector = BarcodeDetector.Builder(this).build()
        barcodeDetector?.setProcessor(
            MultiProcessor
            .Builder(BarcodeTrackerFactory(graphicOverlay, this)).build())
        prepareUI()
    }

    private fun prepareUI() {
        when (deviceType) {
            DeviceType.SMART_LITE -> {
                layoutCamera.visibility = View.INVISIBLE
                layoutScanInstruction.visibility = View.VISIBLE
            }
            DeviceType.SMART_DROID -> {
                layoutCamera.visibility = View.INVISIBLE
                layoutScanInstruction.visibility = View.VISIBLE
            }
            DeviceType.SMARTPHONE -> {
                layoutCamera.visibility = View.VISIBLE
                layoutScanInstruction.visibility = View.INVISIBLE
            }
        }

        when (config.readerMode) {
            QrCodeReaderMode.AUTH_EMPLOYEE -> {
                buttonFinishRead.visibility = View.GONE
                progressBarStepProgress.visibility = View.GONE
                textViewStepDescription.text = "Авторизация сотрудника"
                buttonScanerMenu.visibility = View.INVISIBLE
            }
            QrCodeReaderMode.ACCOUNTING -> {
                buttonFinishRead.visibility = View.VISIBLE
                buttonFinishRead.isEnabled = false
                progressBarStepProgress.visibility = View.VISIBLE
            }
        }

        if (codeHandler.isInfiniteReading) {
            buttonFinishRead.visibility = View.VISIBLE
        } else {
            buttonFinishRead.visibility = View.GONE
        }
    }

    @Throws(SecurityException::class)
    private fun startCameraSource() {
        if (!barcodeDetector!!.isOperational) {
            Toast.makeText(
                this, "Barcode Detector не подключен.",
                Toast.LENGTH_LONG
            ).show()
            return
        } else {
            val display = windowManager.defaultDisplay
            val size = Point()
            display.getSize(size)
            val width: Int = size.x
            val height: Int = size.y
            this.cameraSource = CameraSource.Builder(applicationContext, this.barcodeDetector)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedFps(60.0f)
                .setAutoFocusEnabled(true)
                .setRequestedPreviewSize(width, height)
                .build()
        }
        try {
            cameraPreview?.start(cameraSource, graphicOverlay)
        } catch (e: IOException) {
            cameraSource?.release()
            cameraSource = null
        }
    }

    private fun stopCamera() {
        thread {
            cameraPreview.stop()
        }
    }

    override fun onBarcodeDetected(barcode: Barcode?) {
        if (canReadBarcode) {
            canReadBarcode = false
            runOnUiThread {
                App.log("data detected: ${barcode?.rawValue}")
                barcode?.rawValue?.let { handleCodeValue(it) }
            }
            readDelay.start()
        }
    }

    /**
     * Check is possible start camera, check permission and start it if all is ok.
     * */
    private fun checkCamera() {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.CAMERA),
                LaunchActivityResult.permissions)
        } else {
            startCameraSource()
        }
    }

    private fun setupCodeHandler() {
        codeHandler = config.getCodeHandlerInstance()
        codeHandler.listener = this
        if (codeHandler is StepHandler) {
            (codeHandler as StepHandler).initFirstStep()
            return
        }
    }


    /**
     * Main function of handling scan data by code handler with codec processing
     * @param value scan data
     * */
    private fun handleCodeValue(value: String) {
        if (value.isEmpty()) {
            Logger.logEvent(Logger.Event.CATCH_VALUE, "{empty value}")
            onError("Ошибка сканирования: данные отсутствуют")
        }
        Logger.logEvent(Logger.Event.CATCH_VALUE, value)
        val encoded = Codec.decode(value, AppSettings.get<Codec>(AppSettings.KEY_CODEC))
        codeHandler.handleValue(encoded)
    }

    @SuppressLint("SetTextI18n")
    override fun onNextStep(step: Step) {
        if (codeHandler is StepHandler) {
            textViewStepDescription.text = "${step.id}/${(codeHandler as StepHandler).stepsCount}: ${step.description}"
            textViewError.visibility = View.GONE
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                progressBarStepProgress.setProgress(step.id, true)
            } else {
                progressBarStepProgress.progress = step.id
            }
        }
    }

    /**
     * Create context menu for menu button
     * Menu contains clear list option and can open reader for change status of site.
     * */
    override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_scaner, menu)
    }

    @SuppressLint("SetTextI18n")
    override fun onContextItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.clearList -> {
                if (codeHandler is StepHandler) {
                    (codeHandler as StepHandler).resetSteps()
                }
                universalAdapter.clear()
                canReadBarcode = true
                buttonFinishRead.isEnabled = false
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onError(message: String) {
        showErrorDelay.cancel()
        showErrorDelay.start()
        textViewError.visibility = View.VISIBLE
        textViewError.text = message
    }

    override fun onAllDataCollected() {
        if (universalAdapter.getItems().isEmpty()) {
            onError("Невозможно отправить пустые данные")
            return
        }
        App.log("all data collected")
        readDelay.cancel()
        canReadBarcode = false
        codeHandler.prepare()
        viewModel.startProcess(
            Converter.convertDataToProcessRequestBody(
            codeHandler.baseParams,
            codeHandler.contextParams))
    }

    override fun onStepsFinished() {
        readDelay.cancel()
        canReadBarcode = false
        buttonFinishRead.isEnabled = true
    }

    override fun onDataReaded(code: Code) {
        showFoundedDataDelay.cancel()
        showFoundedDataDelay.start()
        textViewType.text = code.typeDescription
        textViewData1.text = code.data
        layoutFoundedData.visibility = View.VISIBLE
        universalAdapter.addFirst(code)
        recyclerViewFounded.scrollToPosition(0)
    }


    @SuppressLint("SetTextI18n")
    private fun observeViewModel() {
        /**
         * endWork called when app start process success
         * Observer calls functions for save important  or deleted unnecessary data.
         * */
        viewModel.endWork.observe(this, Observer {
            codeHandler.endWork()
            when (config.readerMode) {
                QrCodeReaderMode.AUTH_EMPLOYEE -> {
                    Logger.logEvent(Logger.Event.START_PROCESS_SUCCESS, "Авторизация")
                    startActivity(MainActivity.getInstance(this))
                }
                QrCodeReaderMode.ACCOUNTING -> {
                    Logger.logEvent(Logger.Event.START_PROCESS_SUCCESS, "Учет данных")
                }
            }
            finish()
        })

        viewModel.isError.observe(this, Observer {
            layoutError.visibility = if (it) View.VISIBLE else View.GONE
        })

        viewModel.isLoadingProcess.observe(this, Observer {
            if (it) {
                loading.show()
            } else {
                loading.hide()
            }
        })

        universalAdapter.itemsCountChanged.observe(this, Observer {
            buttonShowReaded.text = "Показать найденные: ${universalAdapter.itemCount}"
        })
    }
}