package com.risco.dxfviewer.data
import android.content.Context
import android.net.Uri
import com.risco.dxfviewer.vm.ViewerViewModel
import java.io.BufferedReader
import java.io.InputStreamReader
object DxfLoader {
  data class Result(val entities: List<ViewerViewModel.Entity>, val layers: Map<String, Boolean>)
  fun load(ctx: Context, uri: Uri): Result {
    ctx.contentResolver.openInputStream(uri).use { input ->
      requireNotNull(input) { "Не удалось открыть файл" }
      val br = BufferedReader(InputStreamReader(input))
      val tokens = generateSequence {
        val code = br.readLine() ?: return@generateSequence null
        val value = br.readLine() ?: return@generateSequence null
        code.trim() to value.trim()
      }.iterator()
      val out = mutableListOf<ViewerViewModel.Entity>()
      val layers = linkedMapOf<String, Boolean>()
      var currentLayer = "0"
      while (tokens.hasNext()) {
        val (code, value) = tokens.next()
        if (code == "0") {
          when (value) {
            "LAYER" -> parseLayer(tokens)?.let { layers[it] = true }
            "LINE" -> out += parseLine(tokens, currentLayer)
            "LWPOLYLINE" -> out += parseLwpolyline(tokens, currentLayer)
            "CIRCLE" -> out += parseCircle(tokens, currentLayer)
            "ARC" -> out += parseArc(tokens, currentLayer)
            "TEXT", "MTEXT" -> out += parseText(tokens, currentLayer, value)
          }
        } else if (code == "8") {
          currentLayer = value; layers.putIfAbsent(currentLayer, true)
        }
      }
      if (layers.isEmpty()) layers["0"] = true
      return Result(out, layers)
    }
  }
  private fun parseLayer(it: Iterator<Pair<String,String>>): String? { var name: String? = null; while (it.hasNext()) { val (c,v)=it.next(); if (c=="0") return name; if (c=="2") name=v }; return name }
  private fun parseLine(it: Iterator<Pair<String,String>>, layer: String): ViewerViewModel.Entity.Line { var x1=0f; var y1=0f; var x2=0f; var y2=0f; var l=layer; while (it.hasNext()) { val (c,v)=it.next(); if (c=="0") return ViewerViewModel.Entity.Line(x1,y1,x2,y2,l); when (c) { "10"->x1=v.toFloatOrNull()?:x1; "20"->y1=v.toFloatOrNull()?:y1; "11"->x2=v.toFloatOrNull()?:x2; "21"->y2=v.toFloatOrNull()?:y2; "8"->l=v } }; return ViewerViewModel.Entity.Line(x1,y1,x2,y2,l) }
  private fun parseLwpolyline(it: Iterator<Pair<String,String>>, layer: String): ViewerViewModel.Entity.Polyline { val pts=mutableListOf<ViewerViewModel.Entity.Pt>(); var closed=false; var l=layer; var lastCode=""; var x=0f; while (it.hasNext()) { val (c,v)=it.next(); if (c=="0") return ViewerViewModel.Entity.Polyline(pts.toList(), closed, l); when (c) { "70"->closed=((v.toIntOrNull()?:0) and 1)==1; "10"->{ x=v.toFloatOrNull()?:x; lastCode=c }; "20"->{ val y=v.toFloatOrNull()?:0f; if (lastCode=="10") pts+=ViewerViewModel.Entity.Pt(x,y) }; "8"->l=v } }; return ViewerViewModel.Entity.Polyline(pts.toList(), closed, l) }
  private fun parseCircle(it: Iterator<Pair<String,String>>, layer: String): ViewerViewModel.Entity.Circle { var cx=0f; var cy=0f; var r=1f; var l=layer; while (it.hasNext()) { val (c,v)=it.next(); if (c=="0") return ViewerViewModel.Entity.Circle(cx,cy,r,l); when (c) { "10"->cx=v.toFloatOrNull()?:cx; "20"->cy=v.toFloatOrNull()?:cy; "40"->r=v.toFloatOrNull()?:r; "8"->l=v } }; return ViewerViewModel.Entity.Circle(cx,cy,r,l) }
  private fun parseArc(it: Iterator<Pair<String,String>>, layer: String): ViewerViewModel.Entity.Arc { var cx=0f; var cy=0f; var r=1f; var a1=0f; var a2=0f; var l=layer; while (it.hasNext()) { val (c,v)=it.next(); if (c=="0") return ViewerViewModel.Entity.Arc(cx,cy,r,a1,a2,l); when (c){ "10"->cx=v.toFloatOrNull()?:cx; "20"->cy=v.toFloatOrNull()?:cy; "40"->r=v.toFloatOrNull()?:r; "50"->a1=v.toFloatOrNull()?:a1; "51"->a2=v.toFloatOrNull()?:a2; "8"->l=v } }; return ViewerViewModel.Entity.Arc(cx,cy,r,a1,a2,l) }
  private fun parseText(it: Iterator<Pair<String,String>>, layer: String, kind: String): ViewerViewModel.Entity.Text { var x=0f; var y=0f; var h=2.5f; var valText=""; var l=layer; while (it.hasNext()) { val (c,v)=it.next(); if (c=="0") return ViewerViewModel.Entity.Text(x,y,h,valText,l); when (c) { "10"->x=v.toFloatOrNull()?:x; "20"->y=v.toFloatOrNull()?:y; "40"->h=v.toFloatOrNull()?:h; "1"->valText=v; "8"->l=v } }; return ViewerViewModel.Entity.Text(x,y,h,valText,l) }
}
