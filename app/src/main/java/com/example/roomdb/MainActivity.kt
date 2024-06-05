package com.example.roomdb

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.room.Room

class MainActivity : ComponentActivity() {

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
                Scaffold(modifier = Modifier.fillMaxSize().safeDrawingPadding().padding(16.dp)) { innerPadding ->
                    HomeScreen()
            }
        }
    }
}

@Composable
fun HomeScreen () {
    val context = LocalContext.current

    val db = Room.databaseBuilder(
        context,
        StudentDB::class.java, "student-db"
    ).allowMainThreadQueries().build()

    var listStudents by remember {
        mutableStateOf(db.studentDAO().getAll())
    }
    var showDialog by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var studentToEdit by remember { mutableStateOf<StudentModel?>(null) }
    var studentToDelete by remember { mutableStateOf<StudentModel?>(null) }
    if (showAddDialog) {
        AddStudentDialog(
            onConfirm = { student ->
                db.studentDAO().insert(student)
                listStudents = db.studentDAO().getAll()
                showAddDialog = false
            },
            onDismiss = {
                showAddDialog = false
            }
        )
    }

    if (showEditDialog && studentToEdit != null) {
        EditStudentDialog(
            student = studentToEdit!!,
            onConfirm = { student ->
                db.studentDAO().update(student)
                listStudents = db.studentDAO().getAll()
                showEditDialog = false
                studentToEdit = null
            },
            onDismiss = {
                showEditDialog = false
                studentToEdit = null
            }
        )
    }
    if (showDialog && studentToDelete != null) {
        DeleteConfirmationDialog(
            onConfirm = {
                db.studentDAO().delete(studentToDelete!!)
                listStudents = db.studentDAO().getAll()
                showDialog = false
                studentToDelete = null
            },
            onDismiss = {
                showDialog = false
                studentToDelete = null
            }
        )
    }
    Column (Modifier.fillMaxWidth()){
        Text(
            text = "Quan ly Sinh vien",
            style = MaterialTheme.typography.titleLarge
        )

        Button(onClick = {
            showAddDialog = true
        }) {
            Text(text = "Thêm SV")
        }

        LazyColumn {

            items(listStudents) { student ->
                Row (
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ){
                    Text(modifier = Modifier.weight(1f), text = student.uid.toString())
                    Text(modifier = Modifier.weight(1f), text = student.hoten.toString())
                    Text(modifier = Modifier.weight(1f), text = student.mssv.toString())
                    Text(modifier = Modifier.weight(1f), text = student.diemTB.toString())
                    Image(
                        painter = painterResource(id = R.drawable.img_edit),
                        contentDescription = "ImageEdit",
                        modifier = Modifier
                            .size(28.dp)
                            .clickable {
                                studentToEdit = student
                                showEditDialog = true
                            }
                    )
                    Image(
                        painter = painterResource(id = R.drawable.img_delete),
                        contentDescription = "ImageDelete",
                        modifier = Modifier
                            .size(28.dp)
                            .clickable {
                                studentToDelete = student
                                showDialog = true
                            }
                    )
                }
                Divider()
            }
        }
    }
}
@Composable
fun AddStudentDialog(
    onConfirm: (StudentModel) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var mssv by remember { mutableStateOf("") }
    var diemTB by remember { mutableStateOf(0f) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Thêm Sinh Viên")
        },
        text = {
            Column {
                TextField(value = name, onValueChange = { name = it }, label = { Text("Họ tên") })
                TextField(value = mssv, onValueChange = { mssv = it }, label = { Text("MSSV") })
                TextField(value = diemTB.toString(), onValueChange = { diemTB = it.toFloatOrNull() ?: 0f }, label = { Text("Điểm TB") })
            }
        },
        confirmButton = {
            Button(onClick = {
                onConfirm(StudentModel(0, name, mssv, diemTB))
            }) {
                Text("Lưu")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}

@Composable
fun EditStudentDialog(
    student: StudentModel,
    onConfirm: (StudentModel) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(student.hoten) }
    var mssv by remember { mutableStateOf(student.mssv) }
    var diemTB by remember { mutableStateOf(student.diemTB) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Sửa Sinh Viên")
        },
        text = {
            Column {
                TextField(value = name.toString(), onValueChange = { name = it }, label = { Text("Họ tên") })
                TextField(value = mssv.toString(), onValueChange = { mssv = it }, label = { Text("MSSV") })
                TextField(value = diemTB.toString(), onValueChange = { diemTB = it.toFloatOrNull() ?: 0f }, label = { Text("Điểm TB") })
            }
        },
        confirmButton = {
            Button(onClick = {
                onConfirm(StudentModel(student.uid, name, mssv, diemTB))
            }) {
                Text("Lưu")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}
@Composable
fun DeleteConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Xác nhận xóa")
        },
        text = {
            Text(text = "Bạn có chắc chắn muốn xóa sinh viên này không?")
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Xóa")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}
@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen()
}