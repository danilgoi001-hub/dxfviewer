package com.risco.dxfviewer
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.risco.dxfviewer.ui.CanvasView
import com.risco.dxfviewer.vm.ViewerViewModel
class MainActivity : ComponentActivity() {
  private val vm: ViewerViewModel by viewModels()
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    vm.contentResolver = contentResolver
    val openDoc = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? -> uri?.let { vm.openUri(this, it) } }
    setContent {
      MaterialTheme {
        Scaffold(topBar = { SmallTopAppBar(title = { Text("DXF Viewer (MVP)") }) }) { padding ->
          Column(Modifier.padding(padding).fillMaxSize()) {
            Row(Modifier.padding(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              Button(onClick = { openDoc.launch(arrayOf("application/dxf","application/octet-stream","*/*")) }) { Text("Открыть DXF…") }
              Text(vm.status)
            }
            CanvasView(vm)
          }
        }
      }
    }
  }
}
