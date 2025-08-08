package com.risco.dxfviewer.tools
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.risco.dxfviewer.vm.ViewerViewModel
class AngleTool(private val vm: ViewerViewModel) {
  private var p1: Offset? = null; private var vertex: Offset? = null; private var p2: Offset? = null
  fun onPick(p: Offset) { when { p1==null -> { p1=p; vm.status="Выберите вершину" } vertex==null -> { vertex=p; vm.status="Выберите вторую точку" } p2==null -> { p2=p; vm.commitMeasurement("Угол", angleDeg(), listOf(p1!!,vertex!!,p2!!)) } else -> { p1=p; vertex=null; p2=null } } }
  fun drawOverlay(scope: DrawScope) { val a=p1; val v=vertex; val b=p2; if(a!=null&&v!=null) scope.drawLine(Color(0xFFE91E63), vm.worldToScreen(v), vm.worldToScreen(a), 2f); if(b!=null&&v!=null) scope.drawLine(Color(0xFFE91E63), vm.worldToScreen(v), vm.worldToScreen(b), 2f) }
  fun label(): Pair<String, Offset>? { val v=vertex?:return null; val deg=angleDeg(); return if(deg.isFinite()) "${format(deg)}°" to v else null }
  private fun angleDeg(): Double { val a=p1?:return Double.NaN; val v=vertex?:return Double.NaN; val b=p2?:return Double.NaN; val ang1 = kotlin.math.atan2((a.y - v.y).toDouble(), (a.x - v.x).toDouble()); val ang2 = kotlin.math.atan2((b.y - v.y).toDouble(), (b.x - v.x).toDouble()); var d = Math.toDegrees(ang2 - ang1); while (d < 0) d += 360.0; if (d>180) d = 360 - d; return d }
  private fun format(v: Double) = "%.2f".format(v)
}
