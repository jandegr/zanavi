/*
 * ZANavi, Zoff Android Navigation system.
 * Copyright (C) 2011-2012 Zoff <zoff@zoff.cc>
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
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import androidx.appcompat.widget.AppCompatImageView;

public class ZANaviBusySpinner extends AppCompatImageView
{
	private final Paint paint;
	private RectF r;
	private int spinner_width;
	static int spinner_size = 35;
	static Boolean active;
	private static RotateAnimation rotateAnim = null;

	public ZANaviBusySpinner(Context context)
	{
		super(context);
		spinner_width = 12;

		if (Navit.metrics.densityDpi >= 320)//&&(Navit.PREF_shrink_on_high_dpi))
		{
			float dpi_factor = ((float) NavitGraphics.Global_want_dpi_other / (float) Navit.metrics.densityDpi);
			//System.out.println("FFFFFF==y:" + dpi_factor);
			spinner_size = (int) (35f / dpi_factor);
			spinner_width = (int) (12f / dpi_factor);
		}

		this.paint = new Paint();
		this.paint.setColor(Color.BLUE);
		this.paint.setAlpha(150);
		rotateAnim = null;
		paint.setAntiAlias(true);
		paint.setStrokeWidth(spinner_width);
		paint.setStyle(Style.STROKE);
		ZANaviBusySpinner.active = false;
		this.r = new RectF(1, 1, 1, 1);
	}

	public ZANaviBusySpinner(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		spinner_width = 12;

		if (Navit.metrics.densityDpi >= 320)//&&(Navit.PREF_shrink_on_high_dpi))
		{
			float dpi_factor = ((float) NavitGraphics.Global_want_dpi_other / (float) Navit.metrics.densityDpi);
			//System.out.println("FFFFFF==y:" + dpi_factor);
			spinner_size = (int) (35f / dpi_factor);
			spinner_width = (int) (12f / dpi_factor);
		}

		this.paint = new Paint();
		this.paint.setColor(Color.BLUE);
		this.paint.setAlpha(150);
		rotateAnim = null;
		paint.setAntiAlias(true);
		paint.setStrokeWidth(spinner_width);
		paint.setStyle(Style.STROKE);
		ZANaviBusySpinner.active = false;
		this.r = new RectF(1, 1, 1, 1);
	}

	public void onSizeChanged(int w, int h, int oldw, int oldh)
	{
		super.onSizeChanged(w, h, oldw, oldh);
		this.r = new RectF((w / 2) - spinner_size, (h / 2) - spinner_size, (w / 2) +
				spinner_size, (h / 2) + spinner_size);
		cancelAnim(); // calc new since w,h has changed - IMHO it will be cancelled ??? jdg
	}

	private void createAnim()
	{
		rotateAnim = new RotateAnimation(0f, 359f, Animation.RELATIVE_TO_SELF,
				0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		rotateAnim.setRepeatMode(Animation.RESTART);
		rotateAnim.setRepeatCount(Animation.INFINITE);
		rotateAnim.setDuration(2600L); // speed of the animation, higher value = lower speed
		rotateAnim.setInterpolator(new LinearInterpolator());
		startAnimation(rotateAnim);
	}


	static void cancelAnim()
	{
		if (rotateAnim != null) //gets called at first start even if there is no anim
		{
			//rotateAnim.cancel(); --> this works only on higher API levels!!
			//  so use these lines instead -->
			rotateAnim.setRepeatMode(Animation.RESTART);
			rotateAnim.setRepeatCount(0);
			rotateAnim.setDuration(2600L); // speed of the animation, higher value = lower speed
			rotateAnim.setInterpolator(new LinearInterpolator());
			// <-- so use these lines instead
			rotateAnim.reset();
			rotateAnim = null;
		}
	}


	public void onDraw(Canvas c)
	{
		//System.out.println("ZANaviBusySpinner onDraw");
		if (ZANaviBusySpinner.active)
		{
			if (rotateAnim == null)
			{
				createAnim();
			}
			//System.out.println("ZANaviBusySpinner draw");
			c.drawArc(this.r, 0, 310, false, this.paint);
		}
	}
}
