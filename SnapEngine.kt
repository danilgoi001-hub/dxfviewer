package com.risco.dxfviewer.geom
import androidx.compose.ui.geometry.Offset
import com.risco.dxfviewer.vm.ViewerViewModel
import kotlin.math.hypot
class SnapEngine {
  private val tol = 8.0
  fun findSnap(p: Offset, entities: List<ViewerViewModel.Entity>): Offset? {
    var best: Offset? = null; var bestDist = Double.POSITIVE_INFINITY
    fun consider(pt: Offset) { val d = hypot((pt.x - p.x).toDouble(), (pt.y - p.y).toDouble()); if (d < bestDist && d < tol) { best = pt; bestDist = d } }
    entities.forEach { e -> when (e) {
      is ViewerViewModel.Entity.Line -> { consider(Offset(e.x1,e.y1)); consider(Offset(e.x2,e.y2)) }
      is ViewerViewModel.Entity.Polyline -> e.points.forEach { consider(Offset(it.x,it.y)) }
      is ViewerViewModel.Entity.Circle -> consider(Offset(e.cx,e.cy))
      is ViewerViewModel.Entity.Arc -> consider(Offset(e.cx,e.cy))
      is ViewerViewModel.Entity.Text -> {} } }
    return best
  }
}
