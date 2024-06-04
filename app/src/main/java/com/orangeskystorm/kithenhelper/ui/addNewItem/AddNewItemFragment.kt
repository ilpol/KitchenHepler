package com.orangeskystorm.kithenhelper.ui.addNewItem

import android.Manifest
import android.app.*
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.speech.RecognizerIntent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.orangeskystorm.kithenhelper.*
import com.orangeskystorm.kithenhelper.db.StoredFridgeItem
import com.orangeskystorm.kithenhelper.db.StoredFridgeItemDAO
import com.orangeskystorm.kithenhelper.db.StoredFridgeItemsDataBase
import com.orangeskystorm.kithenhelper.ui.home.MyAlarm
import com.orangeskystorm.kithenhelper.ui.notifications.StopwatchService.Companion.CHANNEL_ID
import io.reactivex.rxjava3.disposables.CompositeDisposable
import kotlinx.coroutines.launch
import java.io.*
import java.util.*


class AddNewItemFragment : Fragment() {

  // declaring variables
  lateinit var notificationManager: NotificationManager
  lateinit var notificationChannel: NotificationChannel
  lateinit var builder: Notification.Builder
  private val channelId = "i.apps.notifications"
  private val description = "Test notification"

// private var _binding: FragmentNotificationsBinding? = null
  // This property is only valid between onCreateView and
  // onDestroyView.
  //private val binding get() = _binding!!
  val REQUEST_CODE_SPEECH_NAME = 56
  val REQUEST_CODE_SPEECH_DESCRIPTION = 57

  var imgUri: String? = null

  var testImage: ImageView? = null

  var modifiingId: Long? = null

  var fromNotesId: Long? = null

  var nameField: TextView? = null

  var descriptionField: TextView? = null

  private val modifiedFridgeItemViewModel: ModifiedFridgeItemViewModel by activityViewModels()
  private val notesForFridgeViewModel: NotesForFridgeViewModel by activityViewModels()

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {

    // it is a class to notify the user of events that happen.
    // This is how you tell the user that something has happened in the
    // background.
    notificationManager = activity?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    val compositeDisposable = CompositeDisposable()
    val notificationsViewModel =
            ViewModelProvider(this).get(AddNewItemViewModel::class.java)

    // _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
    //val root: View = binding.root

    val root: View = inflater.inflate(com.orangeskystorm.kithenhelper.R.layout.fragment_add_new_item, container, false)


    nameField = root.findViewById(R.id.nameInput) as TextView
    descriptionField = root.findViewById(R.id.descriptionInput) as TextView
    // val speechRecognitionButton = root.findViewById(R.id.speechRecognitionButton) as Button

    val speechRecognitionNameButton = root.findViewById(R.id.speechRecognitionNameButton) as ImageButton
    val speechRecognitionDescriptionButton = root.findViewById(R.id.speechRecognitionDescriptionButton) as ImageButton

    modifiedFridgeItemViewModel.selectedItem.observe(viewLifecycleOwner) { selectedFridgeItem ->
      modifiingId = selectedFridgeItem?.id
      val nameToSet = selectedFridgeItem?.name
      val descriptionToSet = selectedFridgeItem?.description
      if (nameToSet != null) {
        nameField?.setText(nameToSet)
      }
      if (descriptionToSet != null) {
        descriptionField?.setText(descriptionToSet)
      }
    }

    notesForFridgeViewModel.selectedItem.observe(viewLifecycleOwner) { selectedFridgeItem ->
      fromNotesId = selectedFridgeItem?.id
      val nameToSet = selectedFridgeItem?.name
      val descriptionToSet = selectedFridgeItem?.description
      if (nameToSet != null) {
        nameField?.setText(nameToSet)
      }
      if (descriptionToSet != null) {
        descriptionField?.setText(descriptionToSet)
      }

      // notesForFridgeViewModel.selectItem(null)

      val intent = Intent(context, MyAlarm::class.java)

      val productText: String = getString(R.string.product)
      val expiredText: String = getString(R.string.expired)
      val checkTheFridgeText = getString(R.string.checkTheFridge)

      intent.putExtra("title", productText + " " + selectedFridgeItem?.name + " " + expiredText)
      intent.putExtra("description", checkTheFridgeText)

      var pendingIntent: PendingIntent? = null
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        pendingIntent = PendingIntent.getBroadcast(getActivity(), 42, intent, PendingIntent.FLAG_MUTABLE)
      } else {
        pendingIntent = PendingIntent.getBroadcast(getActivity(), 42, intent, PendingIntent.FLAG_ONE_SHOT)
      }
      // val pending = PendingIntent.getBroadcast(context, 42, intent, PendingIntent.FLAG_UPDATE_CURRENT)
      // Schdedule notification
      // Schdedule notification
      val manager: AlarmManager = activity?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
      manager.cancel(pendingIntent)

    }

    fun prepareSpeechData(e: View, requestSpeech: Int) {

      // on below line we are calling speech recognizer intent.
      val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)

      // on below line we are passing language model
      // and model free form in our intent
      intent.putExtra(
        RecognizerIntent.EXTRA_LANGUAGE_MODEL,
        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
      )

      // on below line we are passing our
      // language as a default language.
      intent.putExtra(
        RecognizerIntent.EXTRA_LANGUAGE,
        Locale.getDefault()
      )

      // on below line we are specifying a prompt
      // message as speak to text on below line.
      intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to text")

      // on below line we are specifying a try catch block.
      // in this block we are calling a start activity
      // for result method and passing our result code.
      try {
        startActivityForResult(intent, requestSpeech)
      } catch (e: Exception) {
        // on below line we are displaying error message in toast
        Toast.makeText(getActivity(), "Ошибка распознавания речи", Toast.LENGTH_LONG).show()
      }

    }



    speechRecognitionNameButton.setOnClickListener() { e ->
      prepareSpeechData(e, REQUEST_CODE_SPEECH_NAME)
    }

    speechRecognitionDescriptionButton.setOnClickListener() { e ->
      prepareSpeechData(e, REQUEST_CODE_SPEECH_DESCRIPTION)
    }



    val addItemButton = root.findViewById(R.id.addItemButton) as Button

    val datePicker = root.findViewById(R.id.datePicker) as DatePicker

    val addPhotoButton = root.findViewById(R.id.addPhotoButton) as Button

    addPhotoButton.setOnClickListener() { e ->
      // Request permission
      val permissionGranted = requestCameraPermission()
      if (permissionGranted) {
        // Open the camera interface
        openCameraInterface()
      }
    }

    addItemButton.setOnClickListener() { e ->
      val db: StoredFridgeItemsDataBase? = (activity as FridgeItemDbInstance).getDatabase()

      val storedFridgeItemDao: StoredFridgeItemDAO? = db?.storedFridgeItemDao()

      val fridgeItem = StoredFridgeItem()


      val calendar: Calendar = Calendar.getInstance()
      calendar.set(
        datePicker.year,
        datePicker.month,
        datePicker.dayOfMonth,
        20,
        1,
        0
      )

      fridgeItem.name = nameField?.getText().toString()
      fridgeItem.description = descriptionField?.getText().toString()
      fridgeItem.imgUrl = "fsdfsdf"
      fridgeItem.itemUri = imgUri
      fridgeItem.alarmTime = calendar.timeInMillis

      if (modifiingId != null) {

        fridgeItem.id = modifiingId as Long
        modifiedFridgeItemViewModel.selectItem(null)
        viewLifecycleOwner.lifecycleScope.launch {
          storedFridgeItemDao?.update(fridgeItem)
          // findNavController().navigate(R.id.action_navigation_add_new_item_to_dashboard)
          // getActivity()?.getFragmentManager()?.popBackStack();
          // requireActivity().supportFragmentManager?.popBackStack()
          findNavController().popBackStack()
          findNavController().popBackStack()

        }
      } else {
        viewLifecycleOwner.lifecycleScope.launch {
          storedFridgeItemDao?.insert(fridgeItem)
          // findNavController().navigate(R.id.action_navigation_add_new_item_to_dashboard)
          // getActivity()?.getFragmentManager()?.popBackStack();
          // requireActivity().supportFragmentManager?.popBackStack()
          findNavController().popBackStack()

          if (fromNotesId != null) {
            findNavController().popBackStack()
          }

        }

      }

      setAlarm(calendar.timeInMillis, nameField?.getText().toString())


    }

      return root
  }

  val CAMERA_PERMISSION_CODE = 1001;
  private fun requestCameraPermission(): Boolean {
    var permissionGranted = false
// If system os is Marshmallow or Above, we need to request runtime permission
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
      val cameraPermissionNotGranted = checkSelfPermission(activity as Context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED
      if (cameraPermissionNotGranted){
        val permission = arrayOf(Manifest.permission.CAMERA)
        // Display permission dialog
        requestPermissions(permission, CAMERA_PERMISSION_CODE)
      }
      else{
        // Permission already granted
        permissionGranted = true
      }
    }
    else{
      // Android version earlier than M -&gt; no need to request permission
      permissionGranted = true
    }
    return permissionGranted
  }

  override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
    if (requestCode === CAMERA_PERMISSION_CODE) {
      if (grantResults.size === 1 && grantResults[0] ==    PackageManager.PERMISSION_GRANTED){
        // Permission was granted
        openCameraInterface()
      }
      else{
        // Permission was denied
        showAlert("Camera permission was denied. Unable to take a picture.");
      }
    }
  }


  private fun showAlert(message: String) {
  }


  private val IMAGE_CAPTURE_CODE = 1001
  private var imageUri: Uri? = null
  private fun openCameraInterface() {

    val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
    //Log.d("dfdsf", "cameraIntent = " + cameraIntent)
    startActivityForResult(cameraIntent, IMAGE_CAPTURE_CODE)
  }



  // on below line we are calling on activity result method.
  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)

    // in this method we are checking request
    // code with our result code.
    if (requestCode == REQUEST_CODE_SPEECH_NAME) {
      // on below line we are checking if result code is ok
      if (resultCode == RESULT_OK && data != null) {
        // in that case we are extracting the
        // data from our array list
        val res: ArrayList<String> =
          data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS) as ArrayList<String>

        nameField?.setText(Objects.requireNonNull(res)[0])
        Toast.makeText(getActivity(), "Текс = " + Objects.requireNonNull(res)[0], Toast.LENGTH_LONG)
          .show()
      }
    }

    if (requestCode == REQUEST_CODE_SPEECH_DESCRIPTION) {
      // on below line we are checking if result code is ok
      if (resultCode == RESULT_OK && data != null) {

        // in that case we are extracting the
        // data from our array list
        val res: ArrayList<String> =
          data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS) as ArrayList<String>

        descriptionField?.setText(Objects.requireNonNull(res)[0])

        Toast.makeText(getActivity(), "Текс = " + Objects.requireNonNull(res)[0], Toast.LENGTH_LONG)
          .show()
      }
    }

      if (requestCode == IMAGE_CAPTURE_CODE) {
        // Callback from camera intent
        if (resultCode == RESULT_OK) {
          // Set image captured to image view
          // imageView?.setImageURI(imageUri)
          // imgUrl = imageUri

          val photo: Bitmap? = data!!.extras!!["data"] as Bitmap?
          // imageView.setImageBitmap(photo)
          // Log.d("dfdsf", "photo = " + photo)
          if (photo != null) {
            saveToInternalStorage(photo)
          }

          val cw = ContextWrapper(getActivity()?.getApplicationContext())
          val directory: File = cw.getDir("imageDir", Context.MODE_PRIVATE)

          // loadImageFromStorage(directory)
        } else {
          // Failed to take picture
          showAlert("Failed to take camera picture ")
        }
      }

  }


  private fun saveToInternalStorage(bitmapImage: Bitmap): String? {
    val cw = ContextWrapper(getActivity()?.getApplicationContext())
    val directory: File = cw.getDir("imageDir", Context.MODE_PRIVATE)
    // Create imageDir
    val mypath = File(directory, "profile.jpg")
    imgUri = "profile.jpg"
    var fos: FileOutputStream? = null
    try {
      fos = FileOutputStream(mypath)
      // Use the compress method on the BitMap object to write image to the OutputStream
      bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos)
    } catch (e: java.lang.Exception) {
      e.printStackTrace()
    } finally {
      try {
        fos?.close()
      } catch (e: IOException) {
        e.printStackTrace()
      }
    }
    return directory.getAbsolutePath()
  }


  private fun handleResponse() {
  }
    override fun onDestroyView() {
        super.onDestroyView()
        //_binding = null
    }


  private fun createNotificationChannel() {
    // Create the NotificationChannel, but only on API 26+ because
    // the NotificationChannel class is new and not in the support library
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val name: CharSequence = "R.string.channel_name"
      val description = "R.string.channel_desc"
      val importance = NotificationManager.IMPORTANCE_DEFAULT
      val channel = NotificationChannel(CHANNEL_ID, name, importance)
      channel.description = description

      val notificationManager = getActivity()?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?

      notificationManager?.createNotificationChannel(channel)
    }
  }

  private fun setAlarm(timeInMillis: Long, productName: String) {
    val intent = Intent(context, MyAlarm::class.java)

    val productText: String = getString(R.string.product)
    val expiredText: String = getString(R.string.expired)
    val checkTheFridgeText = getString(R.string.checkTheFridge)

    intent.putExtra("title", productText + " " + productName + " " + expiredText)
    intent.putExtra("description", checkTheFridgeText)
    var pendingIntent: PendingIntent? = null
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      pendingIntent = PendingIntent.getBroadcast(getActivity(), 42, intent, PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
    } else {
      pendingIntent = PendingIntent.getBroadcast(getActivity(), 42, intent, PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_UPDATE_CURRENT)
    }

    // val pending = PendingIntent.getBroadcast(context, 42, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    // Schdedule notification
    // Schdedule notification
    val manager: AlarmManager = activity?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    manager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)

  }
  }
