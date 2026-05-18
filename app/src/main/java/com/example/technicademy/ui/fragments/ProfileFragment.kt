package com.example.technicademy.ui.fragments

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.Outline
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.exifinterface.media.ExifInterface
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.technicademy.R
import com.example.technicademy.data.model.Announcement
import com.example.technicademy.data.model.CourseCancellationRequest
import com.example.technicademy.data.repository.AnnouncementStorage
import com.example.technicademy.data.repository.CancellationRequestStorage
import com.example.technicademy.data.repository.FirestoreUserRepository
import com.example.technicademy.data.ScheduleData
import com.example.technicademy.service.ProfileImageStorageService
import com.example.technicademy.service.UserPreferencesServiceImpl
import com.example.technicademy.ui.MainActivity
import com.example.technicademy.ui.adapters.AnnouncementAdapter
import com.example.technicademy.ui.adapters.CancellationRequestAdapter
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import java.io.File
import java.io.FileOutputStream
import java.net.URL

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private val auth = FirebaseAuth.getInstance()
    private val managerEmail = "dorinbaruch16@gmail.com"

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri ?: return@registerForActivityResult
        val userKey = LoginFragment.getCurrentUserKey(requireContext())
        val userId = auth.currentUser?.uid
        if (userKey.isBlank() || userId.isNullOrBlank()) {
            Toast.makeText(requireContext(), R.string.profile_upload_requires_login, Toast.LENGTH_SHORT).show()
            return@registerForActivityResult
        }
        val file = profileImageFile(requireContext(), userKey)
        try {
            requireContext().contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(file).use { output -> input.copyTo(output) }
            }
            val bitmap = BitmapFactory.decodeFile(file.absolutePath) ?: return@registerForActivityResult
            val orientation = ExifInterface(file.absolutePath).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            val rotated = applyExifOrientation(bitmap, orientation)
            if (rotated != bitmap) bitmap.recycle()
            FileOutputStream(file).use { out -> rotated.compress(Bitmap.CompressFormat.JPEG, 90, out) }
            if (rotated != bitmap) rotated.recycle()

            Toast.makeText(requireContext(), R.string.profile_uploading, Toast.LENGTH_SHORT).show()
            uploadProfileImageToFirebase(file, userKey, userId)
        } catch (_: Exception) {
            Toast.makeText(requireContext(), R.string.profile_upload_failed, Toast.LENGTH_SHORT).show()
        }
    }

    private fun applyExifOrientation(bitmap: Bitmap, orientation: Int): Bitmap {
        val degrees = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> return bitmap
        }
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun uploadProfileImageToFirebase(file: File, userKey: String, userId: String) {
        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(requireContext(), R.string.profile_upload_requires_login, Toast.LENGTH_SHORT).show()
            return
        }
        user.getIdToken(true).addOnCompleteListener { tokenTask ->
            if (!isAdded) return@addOnCompleteListener
            if (!tokenTask.isSuccessful) {
                showUploadError(Exception("בעיית התחברות. התחברי מחדש"))
                saveProfileImageLocally(file, userKey)
                return@addOnCompleteListener
            }
            ProfileImageStorageService.upload(
                imageFile = file,
                userId = userId,
                onSuccess = { downloadUrl ->
                    if (!isAdded) return@upload
                    UserPreferencesServiceImpl.setProfileImagePath(requireContext(), userKey, downloadUrl)
                    view?.let { loadProfileImage(it) }
                    Toast.makeText(requireContext(), R.string.profile_upload_success, Toast.LENGTH_SHORT).show()
                },
                onFailure = { error ->
                    if (!isAdded) return@upload
                    showUploadError(error)
                    saveProfileImageLocally(file, userKey)
                }
            )
        }
    }

    private fun showUploadError(error: Exception) {
        val message = ProfileImageStorageService.formatError(error)
        Toast.makeText(
            requireContext(),
            getString(R.string.profile_upload_failed, message),
            Toast.LENGTH_LONG
        ).show()
    }

    private fun saveProfileImageLocally(file: File, userKey: String) {
        UserPreferencesServiceImpl.setProfileImagePath(requireContext(), userKey, file.absolutePath)
        view?.let {
            loadProfileImage(it)
            Toast.makeText(requireContext(), R.string.profile_upload_saved_local, Toast.LENGTH_LONG).show()
        }
    }

    private fun profileImageFile(context: Context, userKey: String): File {
        val safe = userKey.replace(Regex("[^a-zA-Z0-9]"), "_")
        return File(context.filesDir, "profile_$safe.jpg")
    }

    private fun loadProfileImage(view: View) {
        val iv = view.findViewById<ImageView>(R.id.iv_profile_photo)
        val userKey = LoginFragment.getCurrentUserKey(requireContext())
        val stored = UserPreferencesServiceImpl.getProfileImagePath(view.context, userKey)
        when {
            !stored.isNullOrBlank() && stored.startsWith("http") -> loadImageFromUrl(iv, stored)
            !stored.isNullOrBlank() -> {
                val file = File(stored)
                if (file.exists()) {
                    iv.setImageBitmap(BitmapFactory.decodeFile(file.absolutePath))
                    iv.imageTintList = null
                    applyCircularClip(iv)
                } else {
                    loadProfileImageFromFirebase(iv, userKey)
                }
            }
            else -> loadProfileImageFromFirebase(iv, userKey)
        }
    }

    private fun loadProfileImageFromFirebase(iv: ImageView, userKey: String) {
        val userId = auth.currentUser?.uid
        if (userId.isNullOrBlank()) {
            showDefaultProfileIcon(iv)
            return
        }
        ProfileImageStorageService.fetchDownloadUrl(
            userId = userId,
            onSuccess = { url ->
                if (!isAdded) return@fetchDownloadUrl
                UserPreferencesServiceImpl.setProfileImagePath(requireContext(), userKey, url)
                loadImageFromUrl(iv, url)
            },
            onFailure = { showDefaultProfileIcon(iv) }
        )
    }

    private fun loadImageFromUrl(iv: ImageView, url: String) {
        Thread {
            try {
                val bitmap = URL(url).openStream().use { BitmapFactory.decodeStream(it) }
                if (bitmap != null && isAdded) {
                    requireActivity().runOnUiThread {
                        iv.setImageBitmap(bitmap)
                        iv.imageTintList = null
                        applyCircularClip(iv)
                    }
                } else if (isAdded) {
                    requireActivity().runOnUiThread { showDefaultProfileIcon(iv) }
                }
            } catch (_: Exception) {
                if (isAdded) requireActivity().runOnUiThread { showDefaultProfileIcon(iv) }
            }
        }.start()
    }

    private fun showDefaultProfileIcon(iv: ImageView) {
        iv.setImageResource(R.drawable.ic_profile)
        iv.setColorFilter(0xFFb1baba.toInt())
        applyCircularClip(iv)
    }

    private fun applyCircularClip(iv: ImageView) {
        iv.post {
            iv.outlineProvider = object : ViewOutlineProvider() {
                override fun getOutline(view: View, outline: Outline) {
                    outline.setOval(0, 0, view.width, view.height)
                }
            }
            iv.clipToOutline = true
        }
    }

    private val managerClasses = listOf(
        "בחר שיעור", "קבוצת גנים", "כיתות א'-ב'", "כיתות ג'-ד'", "כיתות ה'-ו'",
        "כיתות ה'-ז'", "כיתות ז'-ט'", "קבוצת ליגה", "קבוצת בוגרים"
    )
    private val classToDays = mapOf(
        "קבוצת גנים" to listOf("שלישי"),
        "כיתות א'-ב'" to listOf("ראשון", "רביעי"),
        "כיתות ג'-ד'" to listOf("ראשון", "רביעי", "חמישי"),
        "כיתות ה'-ו'" to listOf("ראשון", "רביעי"),
        "כיתות ה'-ז'" to listOf("שני", "חמישי"),
        "כיתות ז'-ט'" to listOf("ראשון", "רביעי"),
        "קבוצת ליגה" to listOf("שני", "שלישי", "חמישי"),
        "קבוצת בוגרים" to listOf("ראשון", "שלישי")
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val userKey = LoginFragment.getCurrentUserKey(requireContext())
        val prefs = UserPreferencesServiceImpl
        val name = prefs.getUserName(requireContext(), userKey)
        val username = prefs.getCurrentUserKey(requireContext()).takeIf { it.isNotBlank() }
        val displayName = name.takeIf { it != "אורח" } ?: username ?: "אורח"
        val courses = prefs.getUserCoursesDetails(requireContext(), userKey)
        view.findViewById<TextView>(R.id.tv_profile_name).text = "שלום, $displayName"
        view.findViewById<TextView>(R.id.tv_my_course).text = courses
        loadProfileImage(view)
        view.findViewById<ImageView>(R.id.iv_profile_photo).setOnClickListener { pickImage.launch("image/*") }

        val etBody = view.findViewById<EditText>(R.id.et_announcement_body)
        val btnPublish = view.findViewById<Button>(R.id.btn_publish_announcement)
        val managerTargetLayout = view.findViewById<View>(R.id.manager_target_layout)
        val spinnerManagerClass = view.findViewById<Spinner>(R.id.spinner_manager_class)
        val spinnerManagerDay = view.findViewById<Spinner>(R.id.spinner_manager_day)
        val spinnerManagerTime = view.findViewById<Spinner>(R.id.spinner_manager_time)
        val rvAcademy = view.findViewById<RecyclerView>(R.id.rv_academy_announcements)
        val rvMy = view.findViewById<RecyclerView>(R.id.rv_my_announcements)
        rvAcademy.layoutManager = LinearLayoutManager(requireContext())
        rvMy.layoutManager = LinearLayoutManager(requireContext())

        val currentUserId = auth.currentUser?.uid ?: ""
        val currentUserEmail = auth.currentUser?.email?.takeIf { it.isNotBlank() } ?: auth.currentUser?.uid ?: ""
        val isManager = currentUserEmail == managerEmail

        if (isManager) {
            managerTargetLayout.isVisible = true
            view.findViewById<Button>(R.id.btn_reset_registrations).isVisible = true
            view.findViewById<Button>(R.id.btn_clear_announcements).isVisible = true
            view.findViewById<Button>(R.id.btn_clear_announcements).setOnClickListener {
                AnnouncementStorage.clearAll(requireContext())
                Toast.makeText(requireContext(), "כל המודעות נמחקו", Toast.LENGTH_SHORT).show()
                loadMyAnnouncements(view, currentUserEmail)
                loadAcademyAnnouncements(view)
            }
            view.findViewById<Button>(R.id.btn_reset_registrations).setOnClickListener {
                UserPreferencesServiceImpl.clearAllRegistrations(requireContext())
                UserPreferencesServiceImpl.clearUserCourses(requireContext(), userKey)
                Toast.makeText(requireContext(), "כל ההרשמות אופסו. אפשר להתחיל הרשמות מחדש", Toast.LENGTH_LONG).show()
                val newName = prefs.getUserName(requireContext(), userKey)
                val newDisplay = newName.takeIf { it != "אורח" } ?: username ?: "אורח"
                view.findViewById<TextView>(R.id.tv_profile_name).text = "שלום, $newDisplay"
                view.findViewById<TextView>(R.id.tv_my_course).text =
                    UserPreferencesServiceImpl.NO_COURSE_REGISTERED
                loadAcademyAnnouncements(view)
            }
            spinnerManagerClass.adapter = ArrayAdapter(requireContext(), R.layout.spinner_item_hebrew, managerClasses)
            spinnerManagerClass.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, v: View?, position: Int, id: Long) {
                    val cl = if (position > 0) managerClasses[position] else null
                    val days = (cl?.let { classToDays[it] } ?: emptyList()).let { listOf("בחר יום") + it }
                    spinnerManagerDay.adapter = ArrayAdapter(requireContext(), R.layout.spinner_item_hebrew, days)
                    updateTimeSpinner(spinnerManagerTime, cl, null)
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
            spinnerManagerDay.adapter = ArrayAdapter(requireContext(), R.layout.spinner_item_hebrew, listOf("בחר יום"))
            spinnerManagerDay.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, v: View?, position: Int, id: Long) {
                    val cl = if (spinnerManagerClass.selectedItemPosition > 0) managerClasses[spinnerManagerClass.selectedItemPosition] else null
                    val day = if (position > 0 && spinnerManagerDay.adapter != null) spinnerManagerDay.adapter!!.getItem(position).toString() else null
                    updateTimeSpinner(spinnerManagerTime, cl, day)
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
            spinnerManagerTime.adapter = ArrayAdapter(requireContext(), R.layout.spinner_item_hebrew, listOf("בחר שעה"))
        }

        btnPublish.setOnClickListener {
            val body = etBody.text.toString().trim()
            if (body.isEmpty()) {
                Toast.makeText(requireContext(), "נא למלא תוכן מודעה", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val targetCourseKey: String?
            val targetTimeDisplay: String?
            if (isManager && spinnerManagerClass.selectedItemPosition > 0 && spinnerManagerDay.selectedItemPosition > 0 && spinnerManagerDay.adapter != null) {
                val cl = managerClasses[spinnerManagerClass.selectedItemPosition]
                val day = spinnerManagerDay.adapter!!.getItem(spinnerManagerDay.selectedItemPosition).toString()
                targetCourseKey = "$cl|$day"
                targetTimeDisplay = if (spinnerManagerTime.selectedItemPosition > 0 && spinnerManagerTime.adapter != null)
                    spinnerManagerTime.adapter!!.getItem(spinnerManagerTime.selectedItemPosition).toString() else null
            } else {
                targetCourseKey = null
                targetTimeDisplay = null
            }
            val announcement = Announcement(
                userId = currentUserId,
                userEmail = currentUserEmail,
                userDisplayName = displayName,
                body = body,
                targetCourseKey = targetCourseKey,
                targetTimeDisplay = targetTimeDisplay
            )
            AnnouncementStorage.add(requireContext(), announcement)
            etBody.text.clear()
            Toast.makeText(
                requireContext(),
                if (targetCourseKey != null) "ההודעה נשלחה לנרשמים לחוג ויום שנבחרו" else "המודעה פורסמה לכולם",
                Toast.LENGTH_SHORT
            ).show()
            loadMyAnnouncements(view, currentUserEmail)
            loadAcademyAnnouncements(view)
        }

        loadMyAnnouncements(view, currentUserEmail)
        loadAcademyAnnouncements(view)

        setupCancellationUi(view, userKey, currentUserEmail, displayName, isManager)
        if (isManager) {
            setupManagerCancellationRequests(view)
        }

        view.findViewById<Button>(R.id.btn_logout).setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            GoogleSignIn.getClient(requireActivity(), GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()).signOut()
            UserPreferencesServiceImpl.clearCurrentUser(requireContext())
            (activity as? MainActivity)?.showLoginScreen()
        }
    }

    private fun updateTimeSpinner(spinner: Spinner, className: String?, day: String?) {
        val times = if (className != null && day != null) {
            ScheduleData.allSessions.filter { it.className == className && it.day == day }.map { it.time }.distinct()
        } else emptyList()
        spinner.adapter = ArrayAdapter(requireContext(), R.layout.spinner_item_hebrew, listOf("בחר שעה") + times)
    }

    override fun onResume() {
        super.onResume()
        view?.let { v ->
            val userKey = LoginFragment.getCurrentUserKey(requireContext())
            val email = auth.currentUser?.email?.takeIf { it.isNotBlank() } ?: ""
            val isManager = email == managerEmail
            FirestoreUserRepository.syncFromFirestore(requireContext(), userKey) {
                if (!isAdded) return@syncFromFirestore
                v.findViewById<TextView>(R.id.tv_my_course).text =
                    UserPreferencesServiceImpl.getUserCoursesDetails(requireContext(), userKey)
            }
            v.findViewById<TextView>(R.id.tv_my_course).text =
                UserPreferencesServiceImpl.getUserCoursesDetails(requireContext(), userKey)
            if (email.isNotBlank()) {
                loadMyAnnouncements(v, email)
                loadAcademyAnnouncements(v)
            }
            if (!isManager) {
                refreshCancellationSection(v, userKey, email)
            } else {
                loadManagerCancellationRequests(v)
            }
        }
    }

    private fun setupCancellationUi(
        view: View,
        userKey: String,
        userEmail: String,
        displayName: String,
        isManager: Boolean
    ) {
        val cancelLayout = view.findViewById<View>(R.id.cancel_course_layout)
        if (isManager) {
            cancelLayout.isVisible = false
            return
        }
        refreshCancellationSection(view, userKey, userEmail)
        view.findViewById<Button>(R.id.btn_request_cancellation).setOnClickListener {
            submitCancellationRequest(view, userKey, userEmail, displayName)
        }
    }

    private fun refreshCancellationSection(view: View, userKey: String, userEmail: String) {
        val cancelLayout = view.findViewById<View>(R.id.cancel_course_layout)
        val spinner = view.findViewById<Spinner>(R.id.spinner_cancel_course)
        val statusTv = view.findViewById<TextView>(R.id.tv_cancellation_status)
        val courses = UserPreferencesServiceImpl.getUserCoursesDetails(requireContext(), userKey)
        val hasCourses = courses != UserPreferencesServiceImpl.NO_COURSE_REGISTERED
        cancelLayout.isVisible = hasCourses
        if (!hasCourses) return

        val options = UserPreferencesServiceImpl.getUserCourseOptions(requireContext(), userKey)
        if (options.isEmpty()) {
            cancelLayout.isVisible = false
            return
        }

        val labels = listOf(getString(R.string.cancel_course_placeholder)) + options.map { it.second }
        spinner.adapter = ArrayAdapter(requireContext(), R.layout.spinner_item_hebrew, labels)

        val pending = CancellationRequestStorage.getByUserEmail(requireContext(), userEmail)
            .filter { it.isPending() }
        if (pending.isNotEmpty()) {
            statusTv.isVisible = true
            statusTv.text = pending.joinToString("\n") { req ->
                getString(R.string.cancellation_status_pending, req.courseDisplay)
            }
        } else {
            statusTv.isVisible = false
        }
    }

    private fun submitCancellationRequest(
        view: View,
        userKey: String,
        userEmail: String,
        displayName: String
    ) {
        val spinner = view.findViewById<Spinner>(R.id.spinner_cancel_course)
        if (spinner.selectedItemPosition <= 0) {
            Toast.makeText(requireContext(), R.string.cancellation_select_course, Toast.LENGTH_SHORT).show()
            return
        }
        val options = UserPreferencesServiceImpl.getUserCourseOptions(requireContext(), userKey)
        if (options.isEmpty()) {
            Toast.makeText(requireContext(), R.string.cancellation_no_courses, Toast.LENGTH_SHORT).show()
            return
        }
        val (courseKey, courseDisplay) = options[spinner.selectedItemPosition - 1]
        if (CancellationRequestStorage.hasPendingForCourse(requireContext(), userEmail, courseKey)) {
            Toast.makeText(requireContext(), R.string.cancellation_already_pending, Toast.LENGTH_SHORT).show()
            return
        }
        CancellationRequestStorage.add(
            requireContext(),
            CourseCancellationRequest(
                userKey = userKey,
                userEmail = userEmail,
                userDisplayName = displayName,
                courseKey = courseKey,
                courseDisplay = courseDisplay
            )
        )
        Toast.makeText(requireContext(), R.string.cancellation_request_sent, Toast.LENGTH_LONG).show()
        refreshCancellationSection(view, userKey, userEmail)
    }

    private fun setupManagerCancellationRequests(view: View) {
        view.findViewById<TextView>(R.id.tv_manager_cancellation_title).isVisible = true
        val rv = view.findViewById<RecyclerView>(R.id.rv_cancellation_requests)
        rv.isVisible = true
        rv.layoutManager = LinearLayoutManager(requireContext())
        loadManagerCancellationRequests(view)
    }

    private fun loadManagerCancellationRequests(view: View) {
        val rv = view.findViewById<RecyclerView>(R.id.rv_cancellation_requests)
        val title = view.findViewById<TextView>(R.id.tv_manager_cancellation_title)
        val pending = CancellationRequestStorage.getPending(requireContext())
        title.isVisible = pending.isNotEmpty()
        rv.isVisible = pending.isNotEmpty()
        rv.adapter = CancellationRequestAdapter(pending) { request ->
            CancellationRequestStorage.approve(requireContext(), request)
            Toast.makeText(requireContext(), R.string.cancellation_approved, Toast.LENGTH_SHORT).show()
            loadManagerCancellationRequests(view)
        }
    }

    private fun loadAcademyAnnouncements(view: View) {
        val list = AnnouncementStorage.getTargetedForUser(requireContext())
        view.findViewById<RecyclerView>(R.id.rv_academy_announcements).adapter =
            AnnouncementAdapter(list, AnnouncementAdapter.Mode.HOME)
    }

    private fun loadMyAnnouncements(view: View, userEmail: String) {
        val list = AnnouncementStorage.getByUserEmail(requireContext(), userEmail)
        view.findViewById<RecyclerView>(R.id.rv_my_announcements).adapter =
            AnnouncementAdapter(list, AnnouncementAdapter.Mode.MY_LIST)
    }
}
