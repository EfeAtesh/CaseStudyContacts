@file:Suppress("DEPRECATION")

package com.efeates.casestudy_contacts

import android.Manifest
import android.content.ContentProviderOperation
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.SystemClock
import android.provider.ContactsContract
import android.view.View
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.palette.graphics.Palette
import coil.compose.AsyncImage
import com.airbnb.lottie.compose.*
import com.efeates.casestudy_contacts.ui.theme.CaseStudyContactsTheme
import com.efeates.casestudy_contacts.ui.theme.BluePlatte
import com.kevinnzou.compose.swipebox.SwipeBox
import com.kevinnzou.compose.swipebox.SwipeDirection
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.ByteArrayOutputStream

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )


        )

        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
        val client = OkHttpClient.Builder().addInterceptor(logging).build()
        val retrofit = Retrofit.Builder()
            .baseUrl("http://146.59.52.68:11235/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val userApi = retrofit.create(UserApi::class.java)




        setContent {
            CaseStudyContactsTheme {
                val context = LocalContext.current
                val database = remember { ContactDatabase.getDatabase(context) }
                val contactDao = database.contactDao()
                val scope = rememberCoroutineScope()

                var showAddSheet by remember { mutableStateOf(false) }
                var contactToView by remember { mutableStateOf<Contact?>(null) }
                var contactToEdit by remember { mutableStateOf<Contact?>(null) }
                var contactToDelete by remember { mutableStateOf<Contact?>(null) }

                val contacts by contactDao.getAllContacts().collectAsState(initial = emptyList())

                LaunchedEffect(Unit) {
                    try {
                        val response = userApi.getAllUsers()
                        if (response.isSuccessful) {
                            response.body()?.data?.users?.forEach { user ->
                                val existing = contactDao.getContactByRemoteId(user.id ?: "")
                                contactDao.insertContact(
                                    Contact(
                                        id = existing?.id ?: 0,
                                        remoteId = user.id,
                                        firstName = user.firstName ?: "",
                                        lastName = user.lastName ?: "",
                                        phoneNumber = user.phoneNumber ?: "",
                                        profileImageUrl = user.profileImageUrl,
                                        image = existing?.image
                                    )
                                )
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFFF8F8F8)
                ) {
                    ContactsScreen(
                        contacts = contacts,
                        onAddClick = { showAddSheet = true },
                        onContactClick = { contactToView = it },
                        onEditClick = { contactToEdit = it },
                        onDeleteClick = { contactToDelete = it }
                    )
                }

                if (showAddSheet || contactToView != null || contactToEdit != null) {
                    ModalBottomSheet(
                        onDismissRequest = {
                            showAddSheet = false
                            contactToView = null
                            contactToEdit = null
                        },
                        containerColor = Color.White,
                        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                        dragHandle = { BottomSheetDefaults.DragHandle(color = Color(0xFFE0E0E0)) }
                    ) {
                        var isReadOnly by remember(contactToView) { mutableStateOf(contactToView != null) }
                        val activeContact = contactToView ?: contactToEdit

                        AddContactContent(
                            contact = activeContact,
                            isReadOnly = isReadOnly,
                            userApi = userApi,
                            onEditModeToggle = { isReadOnly = false },
                            onBack = {
                                showAddSheet = false
                                contactToView = null
                                contactToEdit = null
                            },
                            onDelete = { contactToDelete = it }
                        )
                    }
                }

                if (contactToDelete != null) {
                    DeleteConfirmationSheet(
                        onDismiss = { contactToDelete = null },
                        onConfirm = {
                            scope.launch {
                                contactToDelete?.let { contact ->
                                    contact.remoteId?.let { userApi.deleteUser(it) }
                                    contactDao.deleteContact(contact)
                                }
                                contactToDelete = null
                                showAddSheet = false
                                contactToView = null
                                contactToEdit = null
                            }
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteConfirmationSheet(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color(0xFFE0E0E0)) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp, top = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Delete Contact",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF202020)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Are you sure you want to delete this contact?",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF6D6D6D),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFF202020))
                ) {
                    Text("No", color = Color(0xFF202020), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Button(
                    onClick = onConfirm,
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF202020))
                ) {
                    Text("Yes", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun ContactsScreen(
    contacts: List<Contact>,
    onAddClick: () -> Unit,
    onContactClick: (Contact) -> Unit,
    onEditClick: (Contact) -> Unit,
    onDeleteClick: (Contact) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var isSearchFocused by remember { mutableStateOf(false) }
    var searchHistory by remember { mutableStateOf(listOf("Adam", "Jessica", "Tim")) }

    val filteredContacts = if (searchQuery.isEmpty()) {
        emptyList()
    } else {
        contacts.filter {
            it.firstName.contains(searchQuery, ignoreCase = true) ||
            it.lastName.contains(searchQuery, ignoreCase = true)
        }
    }

    val grouped = contacts.groupBy { it.firstName.firstOrNull()?.uppercaseChar() ?: '#' }
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Contacts",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF202020)
            )
            Icon(
                painter = painterResource(id = R.drawable.ic_add),
                contentDescription = "Add Contact",
                tint = BluePlatte,
                modifier = Modifier
                    .size(32.dp)
                    .clickable { onAddClick() }
            )
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
            shadowElevation = 1.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_search),
                    contentDescription = "Search",
                    tint = if (isSearchFocused || searchQuery.isNotEmpty()) Color(0xFF202020) else Color(0xFFB0B0B0),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))

                Box(modifier = Modifier.weight(1f)) {
                    if (searchQuery.isEmpty()) {
                        Text(
                            text = "Search by name",
                            color = Color(0xFFB0B0B0),
                            fontSize = 16.sp
                        )
                    }
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { isSearchFocused = it.isFocused },
                        textStyle = LocalTextStyle.current.copy(fontSize = 16.sp, color = Color(0xFF202020)),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() })
                    )
                }

                if (searchQuery.isNotEmpty()) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_info),
                        contentDescription = "Clear",
                        tint = Color(0xFFB0B0B0),
                        modifier = Modifier
                            .size(16.dp)
                            .clickable { searchQuery = "" }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when {
            isSearchFocused && searchQuery.isEmpty() -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SEARCH HISTORY",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFB0B0B0),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "Clear All",
                        style = MaterialTheme.typography.bodyMedium,
                        color = BluePlatte,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.clickable { searchHistory = emptyList() }
                    )
                }
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(searchHistory) { historyItem ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
                                .clickable { searchQuery = historyItem },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "✕", color = Color(0xFFB0B0B0), fontSize = 12.sp)
                            Spacer(Modifier.width(12.dp))
                            Text(text = historyItem, color = Color(0xFF6D6D6D), fontSize = 14.sp)
                        }
                        HorizontalDivider(color = Color(0xFFF6F6F6))
                    }
                }
            }
            searchQuery.isNotEmpty() -> {
                if (filteredContacts.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_search_not_found),
                            contentDescription = null,
                            tint = Color.Unspecified,
                            modifier = Modifier.size(100.dp)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "No Results",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF202020)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "The user you are looking for could not be found.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF6D6D6D),
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    Text(
                        text = "TOP NAME MATCHES",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFB0B0B0),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(filteredContacts) { contact ->
                            SwipeContactItem(
                                contact = contact,
                                onContactClick = { onContactClick(contact) },
                                onEdit = { onEditClick(contact) },
                                onDelete = { onDeleteClick(contact) }
                            )
                        }
                    }
                }
            }
            contacts.isEmpty() -> { EmptyState(onAddClick) }
            else -> {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    grouped.forEach { (initial, contactsInGroup) ->
                        item {
                            Text(
                                text = initial.toString(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                color = Color(0xFFB0B0B0),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                            HorizontalDivider(color = Color(0xFFF2F2F2), thickness = 1.dp)
                        }
                        items(contactsInGroup) { contact ->
                            SwipeContactItem(
                                contact = contact,
                                onContactClick = { onContactClick(contact) },
                                onEdit = { onEditClick(contact) },
                                onDelete = { onDeleteClick(contact) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SwipeContactItem(
    contact: Contact,
    onContactClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val swipeableState = rememberSwipeableState(0)

    SwipeBox(
        modifier = Modifier.fillMaxWidth(),
        state = swipeableState,
        swipeDirection = SwipeDirection.EndToStart,
        endContentWidth = 120.dp,
        endContent = { state, _ ->
            Row(modifier = Modifier.fillMaxHeight()) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(60.dp)
                        .background(Color(0xFF007AFF))
                        .clickable {
                            coroutineScope.launch { state.animateTo(0) }
                            onEdit()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(painterResource(R.drawable.ic_edit), null, tint = Color.White, modifier = Modifier.size(24.dp))
                }
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(60.dp)
                        .background(Color(0xFFFF3B30))
                        .clickable {
                            coroutineScope.launch { state.animateTo(0) }
                            onDelete()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(painterResource(R.drawable.ic_trash), null, tint = Color.White, modifier = Modifier.size(24.dp))
                }
            }
        }
    ) { _, _, _ ->
        ContactItem(contact, onContactClick)
    }
}

@Composable
fun ContactItem(contact: Contact, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val bitmap = contact.image?.let {
            BitmapFactory.decodeByteArray(it, 0, it.size)
        }

        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.size(48.dp).clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else if (contact.profileImageUrl != null) {
            AsyncImage(
                model = contact.profileImageUrl,
                contentDescription = null,
                modifier = Modifier.size(48.dp).clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier.size(48.dp).clip(CircleShape).background(Color(0xFFF0F9FF)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = contact.firstName.take(1).uppercase(),
                    color = BluePlatte,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = "${contact.firstName} ${contact.lastName}",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF202020)
            )
            Text(
                text = contact.phoneNumber,
                fontSize = 14.sp,
                color = Color(0xFF6D6D6D)
            )
        }
    }
}

@Composable
fun EmptyState(onAddClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().fillMaxHeight(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(120.dp).clip(CircleShape).background(Color(0xFFE0E0E0)),
            contentAlignment = Alignment.Center
        ) {
            Icon(painterResource(R.drawable.ic_contact), null, modifier = Modifier.size(80.dp))
        }
        Spacer(Modifier.height(24.dp))
        Text("No Contacts", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("Contacts you've added will appear here.", textAlign = TextAlign.Center)
        Spacer(Modifier.height(24.dp))
        Text("Create New Contact", color = BluePlatte, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { onAddClick() })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddContactContent(
    contact: Contact? = null,
    isReadOnly: Boolean = false,
    userApi: UserApi,
    onEditModeToggle: () -> Unit = {},
    onBack: () -> Unit,
    onDelete: (Contact) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val contactDao = remember { ContactDatabase.getDatabase(context).contactDao() }

    var firstName by remember(contact) { mutableStateOf(contact?.firstName ?: "") }
    var lastName by remember(contact) { mutableStateOf(contact?.lastName ?: "") }
    var phoneNumber by remember(contact) { mutableStateOf(contact?.phoneNumber ?: "") }
    var selectedImage by remember(contact) { mutableStateOf(contact?.image) }
    var profileImageUrl by remember(contact) { mutableStateOf(contact?.profileImageUrl) }

    var showPhotoSheet by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    var isContactInPhone by remember { mutableStateOf(false) }

    LaunchedEffect(isReadOnly, firstName, lastName, phoneNumber) {
        if (isReadOnly) {
            isContactInPhone = checkContactInPhone(context, firstName, lastName, phoneNumber)
        }
    }

    val saveContact = {
        scope.launch {
            try {
                var finalUrl = profileImageUrl
                
                if (selectedImage != null && selectedImage != contact?.image) {
                    val requestFile = selectedImage!!.toRequestBody("image/*".toMediaTypeOrNull())
                    val body = MultipartBody.Part.createFormData("image", "profile.jpg", requestFile)
                    val uploadResponse = userApi.uploadImage(body)
                    if (uploadResponse.isSuccessful) {
                        finalUrl = uploadResponse.body()?.data?.imageUrl
                    }
                }

                val request = CreateUserRequest(
                    firstName = firstName,
                    lastName = lastName,
                    phoneNumber = phoneNumber,
                    profileImageUrl = finalUrl
                )

                val remoteResponse = if (contact?.remoteId != null) {
                    userApi.updateUser(contact.remoteId, request)
                } else {
                    userApi.createUser(request)
                }

                if (remoteResponse.isSuccessful) {
                    val remoteUser = remoteResponse.body()?.data
                    contactDao.insertContact(
                        Contact(
                            id = contact?.id ?: 0,
                            remoteId = remoteUser?.id ?: contact?.remoteId,
                            firstName = firstName,
                            lastName = lastName,
                            phoneNumber = phoneNumber,
                            image = selectedImage,
                            profileImageUrl = remoteUser?.profileImageUrl ?: finalUrl
                        )
                    )
                    showSuccess = true
                } else {
                    Toast.makeText(context, "Server Error: ${remoteResponse.message()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Network Error", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            context.contentResolver.openInputStream(it)?.use { stream ->
                val bitmap = BitmapFactory.decodeStream(stream)
                val outputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
                selectedImage = outputStream.toByteArray()
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        bitmap?.let {
            val outputStream = ByteArrayOutputStream()
            it.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
            selectedImage = outputStream.toByteArray()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        if (permissions[Manifest.permission.READ_CONTACTS] == true && permissions[Manifest.permission.WRITE_CONTACTS] == true) {
            if (isReadOnly) {
                scope.launch { isContactInPhone = checkContactInPhone(context, firstName, lastName, phoneNumber) }
            }
        }
    }

    if (showSuccess) {
        SuccessScreen()
        LaunchedEffect(Unit) { delay(2000); onBack() }
    } else {
        Column(modifier = Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!isReadOnly) {
                    Text("Cancel", color = BluePlatte, modifier = Modifier.clickable { onBack() })
                } else {
                    Spacer(Modifier.width(48.dp))
                }

                Text(
                    text = if (contact == null) "New Contact" else if (isReadOnly) "Contact" else "Edit Contact",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                Box {
                    if (isReadOnly && contact != null) {
                        IconButton(onClick = { showMenu = true }) { Icon(Icons.Default.MoreVert, "More") }
                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                            DropdownMenuItem(
                                text = { Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Edit")
                                    Spacer(Modifier.weight(1f))
                                    Icon(painterResource(R.drawable.ic_edit), null, modifier = Modifier.size(18.dp))
                                } },
                                onClick = { showMenu = false; onEditModeToggle() }
                            )
                            DropdownMenuItem(
                                text = { Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Delete", color = Color.Red)
                                    Spacer(Modifier.weight(1f))
                                    Icon(painterResource(R.drawable.ic_trash), null, tint = Color.Red, modifier = Modifier.size(18.dp))
                                } },
                                onClick = {
                                    showMenu = false
                                    onDelete(contact)
                                }
                            )
                        }
                    } else {
                        val isDoneEnabled = firstName.isNotBlank() && phoneNumber.isNotBlank()
                        Text(
                            text = "Done",
                            color = if (isDoneEnabled) BluePlatte else Color(0xFFD1D1D1),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable(enabled = isDoneEnabled) { saveContact() }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                var dominantColor by remember { mutableStateOf(Color.Transparent) }

                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .drawBehind {
                            if (dominantColor != Color.Transparent) {
                                drawIntoCanvas { canvas ->
                                    val paint = Paint().asFrameworkPaint()
                                    paint.color = android.graphics.Color.TRANSPARENT
                                    paint.setShadowLayer(
                                        30.dp.toPx(),
                                        0f, 0f,
                                        dominantColor.copy(alpha = 0.5f).toArgb()
                                    )
                                    canvas.nativeCanvas.drawCircle(
                                        size.width / 2,
                                        size.height / 2,
                                        size.width / 2,
                                        paint
                                    )
                                }
                            }
                        }
                        .clip(CircleShape)
                        .background(Color(0xFFE0E0E0)),
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedImage != null) {
                        val bitmap = BitmapFactory.decodeByteArray(selectedImage, 0, selectedImage!!.size)

                        LaunchedEffect(selectedImage) {
                            Palette.from(bitmap).generate { palette ->
                                palette?.dominantSwatch?.rgb?.let { colorInt ->
                                    dominantColor = Color(colorInt)
                                }
                            }
                        }

                        Image(bitmap.asImageBitmap(), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    } else if (profileImageUrl != null) {
                        AsyncImage(
                            model = profileImageUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(painterResource(R.drawable.ic_contact), null, tint = Color.White, modifier = Modifier.size(64.dp))
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = if (selectedImage == null) "Add Photo" else "Change Photo",
                    color = if (isReadOnly) Color.Transparent else BluePlatte,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable(enabled = !isReadOnly) { showPhotoSheet = true }
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            ContactTextField(firstName, { firstName = it }, "First Name", enabled = !isReadOnly)
            Spacer(modifier = Modifier.height(16.dp))
            ContactTextField(lastName, { lastName = it }, "Last Name", enabled = !isReadOnly)
            Spacer(modifier = Modifier.height(16.dp))
            ContactTextField(phoneNumber, { phoneNumber = it }, "Phone Number", enabled = !isReadOnly)

            if (isReadOnly) {
                Spacer(modifier = Modifier.height(48.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth().height(56.dp).clickable {
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                            permissionLauncher.launch(arrayOf(Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS))
                        } else {
                            if (isContactInPhone) {
                                Toast.makeText(context, "Already saved!", Toast.LENGTH_SHORT).show()
                            } else {
                                val success = saveToPhoneContact(context, firstName, lastName, phoneNumber)
                                if (success) {
                                    isContactInPhone = true
                                    Toast.makeText(context, "Saved to device contacts!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    },
                    shape = RoundedCornerShape(28.dp),
                    border = BorderStroke(1.dp, if (isContactInPhone) Color(0xFFF2F2F2) else Color(0xFFE0E0E0)),
                    color = Color.White
                ) {
                    Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        Icon(painterResource(R.drawable.ic_save), null, tint = if (isContactInPhone) Color(0xFFD1D1D1) else Color(0xFFB0B0B0), modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (isContactInPhone) "Contact Saved" else "Save to My Phone Contact", 
                            color = if (isContactInPhone) Color(0xFFD1D1D1) else Color(0xFFB0B0B0), 
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    Icon(painterResource(R.drawable.ic_info), null, tint = Color(0xFFB0B0B0), modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (isContactInPhone) "This contact is already saved your phone." else "Save this contact to your device list.",
                        color = Color(0xFFB0B0B0), 
                        fontSize = 12.sp
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }

        if (showPhotoSheet) {
            ModalBottomSheet(onDismissRequest = { showPhotoSheet = false }, containerColor = Color.White, shape = RoundedCornerShape(24.dp)) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth().height(56.dp).clickable { cameraLauncher.launch(); showPhotoSheet = false },
                        shape = RoundedCornerShape(28.dp),
                        border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
                        color = Color.White
                    ) {
                        Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                            Icon(painterResource(R.drawable.ic_camera), null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Camera", fontWeight = FontWeight.Medium)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth().height(56.dp).clickable { galleryLauncher.launch("image/*"); showPhotoSheet = false },
                        shape = RoundedCornerShape(28.dp),
                        border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
                        color = Color.White
                    ) {
                        Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                            Icon(painterResource(R.drawable.ic_gallery), null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Gallery", fontWeight = FontWeight.Medium)
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Cancel", color = BluePlatte, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { showPhotoSheet = false })
                }
            }
        }
    }
}

@Composable
fun SuccessScreen() {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.done))
    val progress by animateLottieCompositionAsState(composition)
    Column(modifier = Modifier.fillMaxWidth().height(400.dp).background(Color.White), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        LottieAnimation(composition, { progress }, modifier = Modifier.size(150.dp))
        Spacer(Modifier.height(24.dp))
        Text("All Done!", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("New contact saved 🎉", color = Color(0xFF6D6D6D))
    }
}

@Composable
fun ContactTextField(value: String, onValueChange: (String) -> Unit, placeholder: String, enabled: Boolean = true) {
    Surface(
        modifier = Modifier.fillMaxWidth().height(48.dp),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color(0xFFF2F2F2)),
        color = Color.White
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp), contentAlignment = Alignment.CenterStart) {
            if (value.isEmpty()) Text(placeholder, color = Color(0xFFB0B0B0), fontSize = 14.sp)
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                enabled = enabled,
                modifier = Modifier.fillMaxWidth(),
                textStyle = LocalTextStyle.current.copy(fontSize = 14.sp, color = if (enabled) Color.Black else Color(0xFF6D6D6D)),
                singleLine = true
            )
        }
    }
}

fun checkContactInPhone(context: Context, firstName: String, lastName: String, phoneNumber: String): Boolean {
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) return false
    val fullName = "$firstName $lastName".trim()
    val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
    val projection = arrayOf(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER)
    val selection = "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} = ? AND ${ContactsContract.CommonDataKinds.Phone.NUMBER} = ?"
    val args = arrayOf(fullName, phoneNumber)
    
    context.contentResolver.query(uri, projection, selection, args, null)?.use { cursor ->
        return cursor.count > 0
    }
    return false
}

fun saveToPhoneContact(context: Context, firstName: String, lastName: String, phoneNumber: String): Boolean {
    val fullName = "$firstName $lastName".trim()
    val ops = arrayListOf<ContentProviderOperation>()

    ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
        .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
        .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
        .build())

    ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
        .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, fullName)
        .build())

    ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
        .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phoneNumber)
        .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
        .build())

    return try {
        context.contentResolver.applyBatch(ContactsContract.AUTHORITY, ops)
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

fun Color.toArgb(): Int = (alpha * 255.0f + 0.5f).toInt() shl 24 or
        ((red * 255.0f + 0.5f).toInt() shl 16) or
        ((green * 255.0f + 0.5f).toInt() shl 8) or
        (blue * 255.0f + 0.5f).toInt()

fun ByteArray.toRequestBody(mediaType: okhttp3.MediaType? = null): okhttp3.RequestBody = 
    this.toRequestBody(mediaType, 0, this.size)
