package com.risco.dxfviewer.tools
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.risco.dxfviewer.vm.ViewerViewModel
import kotlin.math.hypot
class CircleMeasureTool(private val vm: ViewerViewModel) {
  private var pickedCenter: Offset? = null; private var pickedOnCircle: Offset? = null
  fun onPick(p: Offset) { if(pickedCenter==null){pickedCenter=p; vm.status="Выберите точку на окружности"} else if(pickedOnCircle==null){pickedOnCircle=p; val r=radius()?:0.0; vm.commitMeasurement("Радиус", r, listOf(pickedCenter!!, pickedOnCircle!!)) } else { pickedCenter=p; pickedOnCircle=null } }
  fun drawOverlay(scope: DrawScope) { val c=pickedCenter; val q=pickedOnCircle; if(c!=null) scope.drawCircle(Color(0xFF00E676),5f, vm.worldToScreen(c)); if(c!=null&&q!=null) scope.drawLine(Color(0xFF00E676), vm.worldToScreen(c), vm.worldToScreen(q), 2f) }
  fun label(): Pair<String, Offset>? { val c=pickedCenter; val q=pickedOnCircle; val r=radius()?:return null; if(c==null||q==null) return null; val mid=Offset((c.x+q.x)/2f,(c.y+q.y)/2f); return "R = ${format(r)} ${vm.uiState.value.unitsLabel} / D = ${format(2*r)}" to mid }
  private fun radius(): Double? { val c=pickedCenter; val q=pickedOnCircle; return if(c!=null&&q!=null) hypot((q.x-c.x).toDouble(), (q.y-c.y).toDouble()) else null }
  private fun format(v: Double) = "%.2f".format(v)
}
