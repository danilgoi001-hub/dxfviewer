package com.risco.dxfviewer.tools
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.risco.dxfviewer.vm.ViewerViewModel
import kotlin.math.hypot
class DistanceTool(private val vm: ViewerViewModel) {
  private var p1: Offset? = null; private var p2: Offset? = null
  fun onPick(p: Offset) { if (p1==null){p1=p;p2=null}else if(p2==null){p2=p}else{p1=p;p2=null}; updateStatus() }
  private fun updateStatus() { val d=currentDistance(); vm.status = if (d!=null) "Расстояние: ${format(d)} ${vm.uiState.value.unitsLabel}" else "Выберите 2 точки" }
  private fun currentDistance(): Double? { val a=p1; val b=p2; return if(a!=null&&b!=null) hypot((b.x-a.x).toDouble(), (b.y-a.y).toDouble()) else null }
  fun drawOverlay(scope: DrawScope, vm: ViewerViewModel) { val a=p1; val b=p2; if (a!=null) scope.drawCircle(Color(0xFF64B5F6),6f, vm.worldToScreen(a)); if(a!=null&&b!=null) scope.drawLine(Color(0xFF64B5F6), vm.worldToScreen(a), vm.worldToScreen(b), 2f) }
  fun label(vm: ViewerViewModel): Pair<String, Offset>? { val a=p1; val b=p2; val d=currentDistance()?:return null; if(a==null||b==null) return null; val mid=Offset((a.x+b.x)/2f,(a.y+b.y)/2f); return "${format(d)} ${vm.uiState.value.unitsLabel}" to mid }
  private fun format(v: Double) = "%.2f".format(v)
}
