package com.risco.dxfviewer.ui
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.risco.dxfviewer.tools.*
import com.risco.dxfviewer.vm.ViewerViewModel
@Composable
fun CanvasView(vm: ViewerViewModel) {
  val st by vm.uiState
  var mode by remember { mutableStateOf(ToolMode.Distance) }
  val polyTool = remember { PolylineLengthTool(vm) }
  val circleTool = remember { CircleMeasureTool(vm) }
  val angleTool = remember { AngleTool(vm) }
  val areaTool = remember { AreaTool(vm) }
  Column(Modifier.fillMaxSize()) {
    Row(Modifier.padding(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      AssistChip(onClick = { mode = ToolMode.Distance }, label = { Text("Расстояние") })
      AssistChip(onClick = { mode = ToolMode.PolyLength }, label = { Text("Длина полилинии") })
      AssistChip(onClick = { mode = ToolMode.Circle }, label = { Text("R/D") })
      AssistChip(onClick = { mode = ToolMode.Angle }, label = { Text("Угол") })
      AssistChip(onClick = { mode = ToolMode.Area }, label = { Text("Площадь") })
    }
    Canvas(
      modifier = Modifier.fillMaxSize()
        .onPointerEvent(PointerEventType.Move) { ev -> vm.onPointer(PointerEventType.Move, ev.changes.first().position) }
        .pointerInput(Unit) { detectTransformGestures { _, pan, zoom, _ -> vm.onPanZoom(pan, zoom) } }
        .pointerInput(mode) { detectTapGestures(
          onTap = { pos ->
            val pick = st.snapPoint ?: vm.screenToWorld(pos)
            when (mode) {
              ToolMode.Distance -> vm.distancePick(pick)
              ToolMode.PolyLength -> polyTool.onPick(pick)
              ToolMode.Circle -> circleTool.onPick(pick)
              ToolMode.Angle -> angleTool.onPick(pick)
              ToolMode.Area -> areaTool.onPick(pick)
            }
          },
          onDoubleTap = { if (mode == ToolMode.PolyLength) polyTool.onDoubleTap(); if (mode == ToolMode.Area) areaTool.onDoubleTap() }
        ) }
    ) {
      vm.updateViewportSize(androidx.compose.ui.unit.IntSize(size.width.toInt(), size.height.toInt()))
      drawRect(Color(0xFF101214))
      st.entities.forEach { e ->
        when (e) {
          is ViewerViewModel.Entity.Line -> drawLine(Color.White, vm.worldToScreen(Offset(e.x1, e.y1)), vm.worldToScreen(Offset(e.x2, e.y2)), 1f)
          is ViewerViewModel.Entity.Polyline -> {
            val path = Path(); val pts = e.points
            if (pts.isNotEmpty()) {
              val p0 = vm.worldToScreen(Offset(pts[0].x, pts[0].y)); path.moveTo(p0.x, p0.y)
              for (i in 1 until pts.size) { val s = vm.worldToScreen(Offset(pts[i].x, pts[i].y)); path.lineTo(s.x, s.y) }
              if (e.closed) path.close(); drawPath(path, Color.White)
            }
          }
          is ViewerViewModel.Entity.Circle -> drawCircle(Color.White, vm.scaleLength(e.r), vm.worldToScreen(Offset(e.cx, e.cy)))
          is ViewerViewModel.Entity.Arc -> {
            val path = Path(); val steps = 64
            for (i in 0..steps) {
              val t = e.startAngle + (e.endAngle - e.startAngle) * i / steps
              val x = e.cx + e.r * kotlin.math.cos(Math.toRadians(t.toDouble())).toFloat()
              val y = e.cy + e.r * kotlin.math.sin(Math.toRadians(t.toDouble())).toFloat()
              val s = vm.worldToScreen(Offset(x, y))
              if (i == 0) path.moveTo(s.x, s.y) else path.lineTo(s.x, s.y)
            }
            drawPath(path, Color.White)
          }
          is ViewerViewModel.Entity.Text -> {}
        }
      }
      st.snapPoint?.let { sp -> drawCircle(Color(0xFF00E676), 6f, vm.worldToScreen(sp)) }
      vm.distanceOverlay(this)
      polyTool.drawOverlay(this); circleTool.drawOverlay(this); angleTool.drawOverlay(this); areaTool.drawOverlay(this)
    }
  }
}
enum class ToolMode { Distance, PolyLength, Circle, Angle, Area }
