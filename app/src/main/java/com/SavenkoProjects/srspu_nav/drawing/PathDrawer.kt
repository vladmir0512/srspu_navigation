package com.SavenkoProjects.srspu_nav.drawing

import android.annotation.SuppressLint
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Point
import com.SavenkoProjects.srspu_nav.R
import com.SavenkoProjects.srspu_nav.data.Constants.START_POSITION
import com.SavenkoProjects.srspu_nav.data.Door
import com.SavenkoProjects.srspu_nav.data.Hallway

class PathDrawer {
    @SuppressLint("ResourceAsColor")
	fun drawPath(canvas: Canvas, path: List<Point>) {
        val paint = Paint().apply {
            color = R.color.draw_path
            strokeWidth = 25f
            style = Paint.Style.STROKE
        }
        
        val androidPath = Path()
        if (path.isNotEmpty()) {
            androidPath.moveTo(path[0].x.toFloat(), path[0].y.toFloat())
            for (i in 1 until path.size) {
                androidPath.lineTo(path[i].x.toFloat(), path[i].y.toFloat())
            }
        }
        canvas.drawPath(androidPath, paint)
    }

    fun getPoint(
        id: String,
        doors: Map<String, Door>,
        hallways: Map<String, Hallway>,
        startPosition: List<Int>?
    ): Point? {
        return when {
            id == START_POSITION && startPosition != null -> Point(
                startPosition[0],
                startPosition[1]
            )
            doors.containsKey(id) -> Point(doors[id]!!.position[0], doors[id]!!.position[1])
            hallways.containsKey(id) -> Point(hallways[id]!!.path[0][0], hallways[id]!!.path[0][1])
            else -> null
        }
    }
} 