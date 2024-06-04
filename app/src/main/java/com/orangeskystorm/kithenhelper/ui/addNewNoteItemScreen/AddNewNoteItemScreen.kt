package com.orangeskystorm.kithenhelper.ui.addNewNoteItemScreen

import com.orangeskystorm.kithenhelper.ui.addNewItem.AddNewItemViewModel

// import android.R
//import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
//import com.orangeskystorm.kithenhelper.databinding.FragmentNotificationsBinding

import android.Manifest
// import android.R
import android.app.Activity.RESULT_OK
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
// import androidx.test.core.app.ApplicationProvider.getApplicationContext
import com.orangeskystorm.kithenhelper.db.*
import io.reactivex.rxjava3.disposables.CompositeDisposable
import kotlinx.coroutines.launch
import java.io.*
import java.util.*


class AddNewNoteItemScreen : Fragment() {

    private val fridgeForNotesViewModel: FridgeForNotesViewModel by activityViewModels()

    private val modifiedNotesViewModel: ModifiedNotesViewModel by activityViewModels()


    val REQUEST_CODE_SPEECH_NAME = 56
    val REQUEST_CODE_SPEECH_DESCRIPTION = 57

    var nameField: TextView? = null

    var descriptionField: TextView? = null

    var imgUri: String? = null

    var testImage: ImageView? = null


    var modifiingId: Long? = null

    var fromFridgeId: Long? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val compositeDisposable = CompositeDisposable()
        val notificationsViewModel =
            ViewModelProvider(this).get(AddNewItemViewModel::class.java)

        // _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        //val root: View = binding.root

        val root: View = inflater.inflate(com.orangeskystorm.kithenhelper.R.layout.fragment_add_new_note_item, container, false)


        // val speechRecognitionButton = root.findViewById(R.id.speechRecognitionButton) as Button

        val speechRecognitionNameButton = root.findViewById(R.id.speechRecognitionNameButton) as ImageButton
        val speechRecognitionDescriptionButton = root.findViewById(R.id.speechRecognitionDescriptionButton) as ImageButton

        nameField = root.findViewById(R.id.nameInput) as TextView
        descriptionField = root.findViewById(R.id.descriptionInput) as TextView

        modifiedNotesViewModel.selectedItem.observe(viewLifecycleOwner) { selectedNotesItem ->
            modifiingId = selectedNotesItem?.id
            val nameToSet = selectedNotesItem?.name
            val descriptionToSet = selectedNotesItem?.description
            if (nameToSet != null) {
                nameField?.setText(nameToSet)
            }
            if (descriptionToSet != null) {
                descriptionField?.setText(descriptionToSet)
            }
        }

        fridgeForNotesViewModel.selectedItem.observe(viewLifecycleOwner) { selectedNotesItem ->
            // modifiingId = selectedNotesItem?.id
            nameField?.setText(selectedNotesItem?.name)
            val nameToSet = selectedNotesItem?.name
            val descriptionToSet = selectedNotesItem?.description
            if (nameToSet != null) {
                nameField?.setText(nameToSet)
            }
            if (descriptionToSet != null) {
                descriptionField?.setText(descriptionToSet)
            }

            // fridgeForNotesViewModel.selectItem(null)
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

        // val datePicker = root.findViewById(R.id.datePicker) as DatePicker

        val addPhotoButton = root.findViewById(R.id.addPhotoButton) as Button

        // testImage = root.findViewById(R.id.test) as ImageView

        addPhotoButton.setOnClickListener() { e ->
            // Request permission
            val permissionGranted = requestCameraPermission()
            if (permissionGranted) {
                // Open the camera interface
                openCameraInterface()
            }
        }

        addItemButton.setOnClickListener() { e ->
            // Toast.makeText(getActivity(), "Click on item at", Toast.LENGTH_LONG).show()
            val db: StoredNotesItemsDataBase? = (activity as NotesItemDbInstance).getNotesDatabase()

            val storedNotesItemDao: StoredNotesItemDAO? = db?.storedNotesItemDao()

            val notesItem = StoredNotesItem()

            notesItem.name = nameField?.getText().toString()
            notesItem.description = descriptionField?.getText().toString()
            notesItem.imgUrl = "fsdfsdf"
            notesItem.itemUri = imgUri

            if (modifiingId != null) {
                notesItem.id = modifiingId as Long
                modifiedNotesViewModel.selectItem(null)
                viewLifecycleOwner.lifecycleScope.launch {
                    storedNotesItemDao?.update(notesItem)
                    // findNavController().navigate(R.id.action_navigation_add_new_note_item_to_notes)
                    // getActivity()?.getFragmentManager()?.popBackStack();
                    // requireActivity().supportFragmentManager?.popBackStack()
                    findNavController().popBackStack()
                    findNavController().popBackStack()
                }
            } else {
                viewLifecycleOwner.lifecycleScope.launch {
                    // Log.d("dfdsf", "insert notesItem = " + notesItem)
                    storedNotesItemDao?.insert(notesItem)
                    // findNavController().navigate(R.id.action_navigation_add_new_note_item_to_notes)
                    // getActivity()?.getFragmentManager()?.popBackStack();
                    // requireActivity().supportFragmentManager?.popBackStack()
                    findNavController().popBackStack()
                    if (fromFridgeId != null) {
                        findNavController().popBackStack()
                    }
                }


            }
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
        // Log.d("dfdsf", "cameraIntent = " + cameraIntent)
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

                Log.d("dfdsf", "onActivityResult here =  ")
                // in that case we are extracting the
                // data from our array list
                val res: ArrayList<String> =
                    data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS) as ArrayList<String>

                Log.d("dfdsf", "onActivityResult res =  " + res)

                // on below line we are setting data
                // to our output text view.
//        outputTV.setText(
//          Objects.requireNonNull(res)[0]
//        )

                nameField?.setText(Objects.requireNonNull(res)[0])
                Toast.makeText(getActivity(), "Текс = " + Objects.requireNonNull(res)[0], Toast.LENGTH_LONG)
                    .show()
            }
        }

        if (requestCode == REQUEST_CODE_SPEECH_DESCRIPTION) {
            // on below line we are checking if result code is ok
            if (resultCode == RESULT_OK && data != null) {

                Log.d("dfdsf", "onActivityResult here =  ")
                // in that case we are extracting the
                // data from our array list
                val res: ArrayList<String> =
                    data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS) as ArrayList<String>

                Log.d("dfdsf", "onActivityResult res =  " + res)

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
                Log.d("dfdsf", "photo = " + photo)
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
        Log.d("dfdsf", "saveToInternalStorage bitmapImage = " + bitmapImage)
        // val cw = ContextWrapper(ApplicationProvider.getApplicationContext<Context>())
        val cw = ContextWrapper(getActivity()?.getApplicationContext())
        // path to /data/data/yourapp/app_data/imageDir
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
//    val intent = Intent(this@AddActivity, MainActivity::class.java)
//    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
//    startActivity(intent)
    }
    override fun onDestroyView() {
        super.onDestroyView()
        //_binding = null
    }

    private fun setAlarm(timeInMillis: Long) {
        val alarmManager = getActivity()?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(activity, TimerAlarmHandler::class.java)
        //val intent = getActivity()?.Intent(this, TimerAlarmHandler::class.java)
        var pendingIntent: PendingIntent? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            pendingIntent = PendingIntent.getBroadcast(getActivity(), 0, intent, PendingIntent.FLAG_MUTABLE)
        } else {
            pendingIntent = PendingIntent.getBroadcast(getActivity(), 0, intent, PendingIntent.FLAG_ONE_SHOT)
        }
        /*alarmManager.setRepeating(
            AlarmManager.RTC,
            timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )*/
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
        Toast.makeText(getActivity(), "Alarm is set", Toast.LENGTH_SHORT).show()
    }
}
