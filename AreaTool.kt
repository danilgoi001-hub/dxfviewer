package com.risco.dxfviewer.tools
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.risco.dxfviewer.vm.ViewerViewModel
class AreaTool(private val vm: ViewerViewModel) {
  private val picks = mutableListOf<Offset>(); private var closed = false
  fun onPick(p: Offset) { if (closed) { picks.clear(); closed=false } ; picks += p; vm.status = "Точек: ${picks.size}" }
  fun onDoubleTap() { if (picks.size>=3) { closed=true; val area=polygonArea(picks); vm.commitMeasurement("Площадь", area, picks.toList()) } }
  fun drawOverlay(scope: DrawScope) { if (picks.size<2) return; val path=Path(); val p0=vm.worldToScreen(picks.first()); path.moveTo(p0.x,p0.y); picks.drop(1).forEach { p -> val s=vm.worldToScreen(p); path.lineTo(s.x,s.y) }; if (closed) path.close(); scope.drawPath(path, Color(0xFF4CAF50)) }
  private fun polygonArea(pts: List<Offset>): Double { var s=0.0; for (i in pts.indices) { val a=pts[i]; val b=pts[(i+1)%pts.size]; s += (a.x*b.y - b.x*a.y) } return kotlin.math.abs(s)/2.0 }
}
