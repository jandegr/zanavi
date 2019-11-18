/*
 * ZANavi, Zoff Android Navigation system.
 * Copyright (C) 2013 Zoff <zoff@zoff.cc>
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
import android.util.AttributeSet;
import android.view.View;

public class ZANaviOSDTimeToDest extends View
{

	private final Paint paint = new Paint();
	private final int OSD_element_text_shadow_001 = Color.rgb(255, 255, 255); // text shadow
	private final int OSD_element_text_001 = Color.rgb(244, 180, 0);
	private int OSD_element_text_shadow_width = 1;
	private int h2;
	private int w3 = 10;
	private float textOffset = 0;

	public ZANaviOSDTimeToDest(Context context)
	{
		super(context);
		paint.setTextAlign(Paint.Align.LEFT);
	}

	public ZANaviOSDTimeToDest(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		paint.setTextAlign(Paint.Align.LEFT);
	}

	@Override
	public void onSizeChanged(int w, int h, int oldw, int oldh)
	{
		super.onSizeChanged(w, h, oldw, oldh);
		this.h2 = h / 2;

		OSD_element_text_shadow_width = NavitGraphics.dp_to_px(2);
		w3 = (w / 4);

		int font_size = Navit.find_max_font_size_for_height("+1 20h 10m", h, 95, 9);
		paint.setTextSize(font_size);
		paint.setAntiAlias(true);

		float textHeight = paint.descent() - paint.ascent();
		textOffset = (textHeight / 2) - paint.descent();
	}

	public void onDraw(Canvas c)
	{
		super.onDraw(c);
		int dest_valid = NavitGraphics.CallbackDestinationValid2();

		try
		{
			if ((Navit.OSD_route_001.arriving_secs_to_dest_valid) && (dest_valid > 0))
			{
				String my_text = Navit.OSD_route_001.arriving_secs_to_dest;

				paint.setColor(OSD_element_text_shadow_001);
				paint.setStrokeWidth(OSD_element_text_shadow_width);
				paint.setStyle(Paint.Style.STROKE);
				c.drawText(my_text, w3, h2 + textOffset, paint);

				paint.setColor(OSD_element_text_001);
				paint.setStrokeWidth(3);
				paint.setStyle(Paint.Style.FILL);
				c.drawText(my_text, w3, h2 + textOffset, paint);
			}
			else
			{
				c.drawColor(Color.TRANSPARENT);
			}
		}
		catch (Exception e)
		{
			//e.printStackTrace();
		}

		// System.out.println("onDraw:OSDTimeToDest");
	}
}
