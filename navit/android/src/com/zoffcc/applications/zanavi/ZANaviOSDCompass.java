/*
 * ZANavi, Zoff Android Navigation system.
 * Copyright (C) 2013 - 2015 Zoff <zoff@zoff.cc>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * version 2 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the
 * Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
 * Boston, MA  02110-1301, USA.
 */

package com.zoffcc.applications.zanavi;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import androidx.appcompat.widget.AppCompatImageView;

public class ZANaviOSDCompass extends AppCompatImageView
{
	private int w = 10;
	private int h = 10;
	private float w2 = w / 2;
	private float h2 = h / 2;
	private final Bitmap bitmap_measure;
	private final Paint paint_2 = new Paint();
	private final Paint paint_3 = new Paint();
	private int cx = 0;
	private int cy = 0;

	public ZANaviOSDCompass(Context context)
	{
		super(context);

		Resources res = getResources();
		bitmap_measure = BitmapFactory.decodeResource(res, R.drawable.zanavi_measure);
		paint_2.setDither(true);
		paint_2.setAntiAlias(true);

		paint_3.setColor(0xffcc0000);
		paint_3.setStyle(Paint.Style.FILL);
		// paint_3.setStrokeWidth(4);
	}

	public ZANaviOSDCompass(Context context, AttributeSet attrs)
	{
		super(context, attrs);

		Resources res = getResources();
		bitmap_measure = BitmapFactory.decodeResource(res, R.drawable.zanavi_measure);
		paint_2.setDither(true);
		paint_2.setAntiAlias(true);

		paint_3.setColor(0xffcc0000);
		paint_3.setStyle(Paint.Style.FILL);
		// paint_3.setStrokeWidth(4);
	}

	@Override
	public void onSizeChanged(int w1, int h1, int oldw, int oldh)
	{
		super.onSizeChanged(w1, h1, oldw, oldh);
		this.w = w1;
		this.h = h1;
		this.w2 = w / 2;
		this.h2 = h / 2;

		cx = (w - bitmap_measure.getWidth()) / 2;
		cy = (h - bitmap_measure.getHeight()) / 2;
	}

	public void onDraw(Canvas c)
	{
		if (NavitAndroidOverlay.measure_mode)
		{
			super.onDraw(c);
			// c.drawColor(0xFFAAAAAA, PorterDuff.Mode.CLEAR);
			c.drawCircle(w2, h2, w2, paint_3);
			c.drawBitmap(bitmap_measure, cx, cy, paint_2);
		}
		else
		{
			c.rotate(Navit.OSD_compass.angle_north, w2, h2);
			super.onDraw(c);
		}

		// System.out.println("onDraw:OSDCompass");
	}
}
