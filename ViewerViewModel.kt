package com.risco.dxfviewer.vm
import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.ViewModel
import com.risco.dxfviewer.data.DxfLoader
import com.risco.dxfviewer.geom.SnapEngine
import com.risco.dxfviewer.tools.DistanceTool
import kotlin.math.max
class ViewerViewModel : ViewModel() {
  data class UiState(
    val entities: List<Entity> = emptyList(),
    val layers: Map<String, Boolean> = emptyMap(),
    val scale: Float = 1f,
    val origin: Offset = Offset.Zero,
    val cursorWorld: Offset? = null,
    val snapPoint: Offset? = null,
    val unitsLabel: String = "mm"
  )
  sealed class Entity {
    data class Line(val x1: Float, val y1: Float, val x2: Float, val y2: Float, val layer: String = "0") : Entity()
    data class Polyline(val points: List<Pt>, val closed: Boolean, val layer: String = "0") : Entity()
    data class Circle(val cx: Float, val cy: Float, val r: Float, val layer: String = "0") : Entity()
    data class Arc(val cx: Float, val cy: Float, val r: Float, val startAngle: Float, val endAngle: Float, val layer: String = "0") : Entity()
    data class Text(val x: Float, val y: Float, val height: Float, val value: String, val layer: String = "0") : Entity()
    data class Pt(val x: Float, val y: Float)
  }
  val uiState = mutableStateOf(UiState())
  var status: String = "Файл не загружен"; private set
  lateinit var contentResolver: ContentResolver
  private var viewport = IntSize(1, 1)
  private val snapEngine = SnapEngine()
  private val distanceTool = DistanceTool(this)
  var lastExportBitmap: ImageBitmap? = null
  fun openUri(ctx: Context, uri: Uri) {
    runCatching {
      val res = DxfLoader.load(ctx, uri)
      uiState.value = uiState.value.copy(entities = res.entities, layers = res.layers)
      status = "Загружено: ${res.entities.size} сущ., слоёв: ${res.layers.size}"
      fitToView()
    }.onFailure { e -> status = "Ошибка: ${e.message}" }
  }
  fun updateViewportSize(size: IntSize) { viewport = size }
  fun onPanZoom(pan: Offset, zoom: Float) {
    val st = uiState.value; val newScale = (st.scale * zoom).coerceIn(0.1f, 1000f)
    uiState.value = st.copy(origin = st.origin + pan, scale = newScale)
  }
  fun onPointer(eventType: PointerEventType, posScreen: Offset) {
    val w = screenToWorld(posScreen); val st = uiState.value
    val visible = st.entities.filter { st.layers.getOrDefault(layerOf(it), true) }
    val snap = snapEngine.findSnap(w, visible)
    uiState.value = st.copy(cursorWorld = w, snapPoint = snap)
    if (eventType == PointerEventType.Press) { val p = snap ?: w; distanceTool.onPick(p) }
  }
  fun worldToScreen(p: Offset): Offset { val st = uiState.value; return Offset(st.origin.x + p.x * st.scale, st.origin.y + (-p.y) * st.scale + viewport.height/2f) }
  fun screenToWorld(p: Offset): Offset { val st = uiState.value; return Offset((p.x - st.origin.x) / st.scale, -((p.y - st.origin.y - viewport.height/2f) / st.scale)) }
  fun scaleLength(l: Float): Float = l * uiState.value.scale
  private fun fitToView() {
    val ents = uiState.value.entities; if (ents.isEmpty()) return
    var minX = Float.POSITIVE_INFINITY; var minY = Float.POSITIVE_INFINITY; var maxX = Float.NEGATIVE_INFINITY; var maxY = Float.NEGATIVE_INFINITY
    fun box(x: Float,y: Float){ minX=minOf(minX,x); maxX=maxOf(maxX,x); minY=minOf(minY,y); maxY=maxOf(maxY,y) }
    ents.forEach { e -> when (e) {
      is Entity.Line -> { box(e.x1,e.y1); box(e.x2,e.y2) }
      is Entity.Polyline -> e.points.forEach { box(it.x,it.y) }
      is Entity.Circle -> { box(e.cx-e.r,e.cy-e.r); box(e.cx+e.r,e.cy+e.r) }
      is Entity.Arc -> { box(e.cx-e.r,e.cy-e.r); box(e.cx+e.r,e.cy+e.r) }
      is Entity.Text -> box(e.x, e.y) } }
    val w = max(1f, maxX - minX); val h = max(1f, maxY - minY)
    val s = 0.9f * minOf(viewport.width / w, viewport.height / h)
    uiState.value = uiState.value.copy(scale = s, origin = Offset(-minX*s + (viewport.width - w*s)/2f, (maxY)*s + (viewport.height - h*s)/2f))
  }
  fun toggleLayer(name: String, value: Boolean) { val st = uiState.value; uiState.value = st.copy(layers = st.layers.toMutableMap().apply { this[name] = value }) }
  fun distancePick(p: Offset) { distanceTool.onPick(p) }
  fun distanceOverlay(scope: androidx.compose.ui.graphics.drawscope.DrawScope) { distanceTool.drawOverlay(scope, this) }
  fun currentDistanceLabel(): Pair<String, Offset>? = distanceTool.label(this)
  fun commitMeasurement(kind: String, value: Double, points: List<Offset>) { status = "$kind: ${"%.2f".format(value)} ${uiState.value.unitsLabel}" }
  fun layerOf(e: Entity): String = when (e) { is Entity.Line->e.layer; is Entity.Polyline->e.layer; is Entity.Circle->e.layer; is Entity.Arc->e.layer; is Entity.Text->e.layer }
  fun captureFrame() { val bmp = Bitmap.createBitmap(1920,1080,Bitmap.Config.ARGB_8888); lastExportBitmap = bmp.asImageBitmap() }
}
