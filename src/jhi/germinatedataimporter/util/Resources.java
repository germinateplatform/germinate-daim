/**
 * Copyright 2017 Information and Computational Sciences,
 * The James Hutton Institute.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jhi.germinatedataimporter.util;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

import java.io.*;
import java.util.*;

import jhi.germinatedataimporter.gui.*;

/**
 * {@link Resources} contains other classes to handle {@link Font}s, {@link Image}s and {@link Color}s
 *
 * @author Sebastian Raubach
 */
public class Resources
{
	private static int zoomFactor = 200;

	/**
	 * Returns the zoom factor that was selected during application start <p> The return value will be one of the following values: <ul> <li>100: 1.0x
	 * zoom</li> <li>150: 1.5x zoom</li> <li>200: 2.0x zoom</li> </ul>
	 *
	 * @return The zoom factor that was selected during application start
	 */
	public static int getZoomFactor()
	{
		return zoomFactor;
	}

	/**
	 * Initializes the {@link Resources} class. Will load the necessary {@link Image}s and {@link Color}s
	 */
	public static void initialize()
	{
		Images.initialize();
		Colors.initialize();
	}

	/**
	 * Disposes all {@link Image}s that were loaded during execution (if they haven't already been disposed)
	 */
	public static void disposeResources()
	{
		Images.disposeAll();
		Colors.disposeAll();
	}

	/**
	 * {@link Fonts} is a utility class to handle {@link Font}s
	 *
	 * @author Sebastian Raubach
	 */
	public static class Fonts
	{
		/**
		 * Applies the given {@link Font} size to the given {@link Control}. A new {@link Font} instance is created internally, but disposed via a
		 * {@link Listener} for {@link SWT#Dispose} attached to the {@link Control}.
		 *
		 * @param control  The {@link Control}
		 * @param fontSize The {@link Font} size
		 */
		public static void applyFontSize(Control control, int fontSize)
		{
			/* Increase the font size */
			FontData[] fontData = control.getFont().getFontData();
			fontData[0].setHeight(fontSize);
			final Font font = new Font(control.getShell().getDisplay(), fontData[0]);
			control.setFont(font);

			control.addListener(SWT.Dispose, event -> {
				if (!font.isDisposed())
					font.dispose();
			});
		}
	}

	/**
	 * {@link Images} is a utility class to handle {@link Image}s
	 *
	 * @author Sebastian Raubach
	 */
	public static class Images
	{
		private static final String             IMAGE_PATH  = "img";
		/** Image cache */
		private static final Map<String, Image> IMAGE_CACHE = new HashMap<>();
		public static Image JHI_LOGO;
		public static Image GERMINATE_LOGO;
		public static Image GERMINATE_TRAY;
		public static Image HUTTON_BACKGROUND;
		public static Image TWITTER_LOGO;
		public static Image SETTINGS;
		public static Image EMAIL;
		public static Image WEB;
		public static Image DATABASE_CONNECT;
		public static Image DATABASE_IMPORT;
		public static Image DATABASE_UPDATE;
		public static Image DELETE;
		public static Image ADD;
		public static Image WARNING;
		public static Image OK;
		public static Image DATE;
		public static Image KEY;
		public static Image FOLDER;
		public static Image UNDO;
		public static Image NUMBER_RANGE;
		public static Image MANUAL_ENTRY;
		public static Image REGEX;

		private static void initialize()
		{
			JHI_LOGO = loadImage("res/jhi.png");

			GERMINATE_LOGO = loadImage("res/germinate.png");
			GERMINATE_TRAY = loadImage("res/logo.png");

			HUTTON_BACKGROUND = loadImage("res/huttonlarge.png");

			TWITTER_LOGO = loadImage("icons/twitter.png");

			SETTINGS = loadImage("icons/cog.png");
			EMAIL = loadImage("icons/email.png");
			WEB = loadImage("icons/world.png");
			DATABASE_CONNECT = loadImage("icons/database-connect.png");
			DATABASE_IMPORT = loadImage("icons/database-go.png");
			DATABASE_UPDATE = loadImage("icons/database-refresh.png");
			DELETE = loadImage("icons/cross.png");
			ADD = loadImage("icons/add.png");
			WARNING = loadImage("icons/error.png");
			OK = loadImage("icons/accept.png");
			DATE = loadImage("icons/date.png");
			KEY = loadImage("icons/table-relationship.png");
			FOLDER = loadImage("icons/folder.png");
			UNDO = loadImage("icons/undo.png");
			NUMBER_RANGE = loadImage("icons/number-range.png");
			MANUAL_ENTRY = loadImage("icons/keyboard.png");
			REGEX = loadImage("icons/tag.png");
		}

		/**
		 * Returns a copy of the given {@link Image} with the given alpha value
		 *
		 * @param image The original {@link Image}
		 * @param alpha The alpha value
		 * @return The copy of the given {@link Image} with the given alpha value
		 */
		@SuppressWarnings("unused")
		public static Image applyAlpha(Image image, int alpha)
		{
			String name = image.toString() + "_" + alpha;

			Image result = IMAGE_CACHE.get(name);

			if (result == null)
			{
				ImageData data = image.getImageData();
				data.alpha = alpha;
				result = new Image(null, data);

				IMAGE_CACHE.put(name, result);
			}

			return result;
		}

		/**
		 * Loads and returns the {@link Image} with the given name
		 *
		 * @param name The image name
		 * @return The {@link Image} object
		 */
		public static Image loadImage(String name)
		{
			/* Check if we've already created that Image before */
			Image newImage = IMAGE_CACHE.get(name);
			if (newImage == null || newImage.isDisposed())
			{
				/* If not, try to load it */
				try
				{
					/* Check if this code is in the jar */
					if (GerminateDataImporter.WITHIN_JAR)
					{
						newImage = new Image(null, (ImageDataProvider) zoom ->
						{
							zoomFactor = Math.min(zoomFactor, zoom);

							String path = IMAGE_PATH + "/" + zoom + "-" + name;

							InputStream stream = GerminateDataImporter.class.getClassLoader().getResourceAsStream(path);
							if (stream == null)
							{
								/* Check if the base image exists (zoom=100) */
								path = IMAGE_PATH + "/100-" + name;
								stream = GerminateDataImporter.class.getClassLoader().getResourceAsStream(path);

								/* If that doesn't exist, try without a zoom level */
								if (stream == null)
								{
									path = IMAGE_PATH + "/" + name;
									stream = GerminateDataImporter.class.getClassLoader().getResourceAsStream(path);
								}
							}

							Image image = new Image(null, stream);
							ImageData data = image.getImageData();
							image.dispose();

							return data;
						});
					}
					/* Else, we aren't using a jar */
					else
					{
						newImage = new Image(null, (ImageFileNameProvider) zoom ->
						{
							zoomFactor = Math.min(zoomFactor, zoom);

							String path = IMAGE_PATH + "/" + zoom + "-" + name;

							/* Try to fall back */
							if (!new File(path).exists())
							{
								/* Check if the base image exists (zoom=100) */
								path = IMAGE_PATH + "/100-" + name;

								/* If that doesn't exist, try without a zoom level */
								if (!new File(path).exists())
									path = IMAGE_PATH + "/" + name;
							}

							return path;
						});
					}

                    /* Remember that we loaded this image */
					IMAGE_CACHE.put(name, newImage);
				}
				catch (SWTException ex)
				{
					return null;
				}
			}

			return newImage;
		}

		private static void disposeAll()
		{
			IMAGE_CACHE.values().stream().filter(img -> !img.isDisposed()).forEach(Image::dispose);
			IMAGE_CACHE.clear();
		}
	}

	/**
	 * {@link Colors} is a utility class to handle {@link Color}s
	 *
	 * @author Sebastian Raubach
	 */
	public static class Colors
	{
		/** Color cache */
		private static final Map<String, Color> COLOR_CACHE = new HashMap<>();
		public static Color DARK_GREY;

		private static void initialize()
		{
			Colors.DARK_GREY = loadColor("#444444");
		}

		/**
		 * Loads and returns the {@link Color} with the given hex
		 *
		 * @param color The color hex
		 * @return The {@link Color} object
		 */
		public static Color loadColor(String color)
		{
			Color newColor = COLOR_CACHE.get(color);
			if (newColor == null || newColor.isDisposed())
			{
				java.awt.Color col;
				try
				{
					col = java.awt.Color.decode(color);
				}
				catch (Exception e)
				{
					col = java.awt.Color.WHITE;
				}
				int red = col.getRed();
				int blue = col.getBlue();
				int green = col.getGreen();

				newColor = new Color(null, red, green, blue);

				COLOR_CACHE.put(color, newColor);
			}

			return newColor;
		}

		private static void disposeAll()
		{
			COLOR_CACHE.values()
					   .stream()
					   .filter(col -> !col.isDisposed())
					   .forEach(Color::dispose);

			COLOR_CACHE.clear();
		}
	}
}
