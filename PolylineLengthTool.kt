package com.risco.dxfviewer.tools
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.risco.dxfviewer.vm.ViewerViewModel
import kotlin.math.hypot
class PolylineLengthTool(private val vm: ViewerViewModel) {
  private val picks = mutableListOf<Offset>()
  fun onPick(p: Offset) { picks += p; vm.status = "Точек: ${picks.size}. Длина: ${format(total())} ${vm.uiState.value.unitsLabel}" }
  fun onDoubleTap() { if (picks.size>=2) { vm.commitMeasurement("Длина полилинии", total(), picks.toList()); picks.clear() } }
  fun drawOverlay(scope: DrawScope) { if (picks.isEmpty()) return; var prev = picks.first(); picks.drop(1).forEach { cur -> scope.drawLine(Color(0xFFFFC107), vm.worldToScreen(prev), vm.worldToScreen(cur), 2f); prev = cur }; scope.drawCircle(Color(0xFFFFC107),5f, vm.worldToScreen(prev)) }
  private fun total(): Double { var sum=0.0; var prev: Offset?=null; for (p in picks){ if(prev!=null) sum+=hypot((p.x-prev!!.x).toDouble(), (p.y-prev!!.y).toDouble()); prev=p } return sum }
  private fun format(v: Double) = "%.2f".format(v)
}
